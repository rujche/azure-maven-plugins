/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.redis;

import com.azure.resourcemanager.redis.RedisManager;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureBaseResource;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.redis.model.PricingTier;
import lombok.Data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class RedisCacheDraft extends RedisCache implements AzResource.Draft<RedisCache, com.azure.resourcemanager.redis.models.RedisCache> {
    @Nullable
    private Config config;

    RedisCacheDraft(@Nonnull String name, @Nonnull String resourceGroup, @Nonnull RedisCacheModule module) {
        super(name, resourceGroup, module);
        this.setStatus(IAzureBaseResource.Status.DRAFT);
    }

    @Override
    public void reset() {
        this.config = null;
    }

    @Override
    public com.azure.resourcemanager.redis.models.RedisCache createResourceInAzure() {
        final String redisName = this.getName();
        final RedisManager manager = Objects.requireNonNull(this.getParent().getRemote());
        final com.azure.resourcemanager.redis.models.RedisCache.DefinitionStages.WithSku toCreate =
            manager.redisCaches().define(redisName)
                .withRegion(this.getRegion().getName())
                .withExistingResourceGroup(this.getResourceGroup());
        com.azure.resourcemanager.redis.models.RedisCache.DefinitionStages.WithCreate withCreate;
        final PricingTier tier = this.getPricingTier();
        if (tier.isStandard()) {
            withCreate = toCreate.withStandardSku(tier.getSize());
        } else if (tier.isPremium()) {
            withCreate = toCreate.withPremiumSku(tier.getSize());
        } else {
            withCreate = toCreate.withBasicSku(tier.getSize());
        }
        if (this.isNonSslPortEnabled()) {
            withCreate = withCreate.withNonSslPort();
        }
        final IAzureMessager messager = AzureMessager.getMessager();
        messager.info(AzureString.format("Start creating Redis Cache({0})...", redisName));
        final com.azure.resourcemanager.redis.models.RedisCache redis = withCreate.create();
        messager.success(AzureString.format("Redis Cache({0}) is successfully created.", redisName));
        return redis;
    }

    @Override
    public com.azure.resourcemanager.redis.models.RedisCache updateResourceInAzure(@Nonnull com.azure.resourcemanager.redis.models.RedisCache origin) {
        throw new AzureToolkitRuntimeException("not supported");
    }

    private synchronized Config ensureConfig() {
        this.config = Optional.ofNullable(this.config).orElseGet(Config::new);
        return this.config;
    }

    public void setPricingTier(@Nonnull PricingTier tier) {
        this.ensureConfig().setPricingTier(tier);
    }

    public PricingTier getPricingTier() {
        return Optional.ofNullable(config).map(Config::getPricingTier).orElseGet(super::getPricingTier);
    }

    public void setRegion(@Nonnull Region region) {
        this.ensureConfig().setRegion(region);
    }

    @Nonnull
    public Region getRegion() {
        return Objects.requireNonNull(Optional.ofNullable(config).map(Config::getRegion).orElseGet(super::getRegion));
    }

    @Override
    public boolean isNonSslPortEnabled() {
        return Optional.ofNullable(config).map(Config::isEnableNonSslPort).orElseGet(super::isNonSslPortEnabled);
    }

    public void setNonSslPortEnabled(boolean enabled) {
        this.ensureConfig().setEnableNonSslPort(enabled);
    }

    /**
     * {@code null} means not modified for properties
     */
    @Data
    private static class Config {
        private Region region;
        private PricingTier pricingTier;
        private boolean enableNonSslPort;
    }
}