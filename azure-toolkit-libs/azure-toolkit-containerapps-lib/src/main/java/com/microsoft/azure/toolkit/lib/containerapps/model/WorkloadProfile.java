/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.containerapps.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkloadProfile {
    public static final String CONSUMPTION = "Consumption";
    public static final WorkloadProfile CONSUMPTION_PROFILE = WorkloadProfile.builder().name(CONSUMPTION).workloadProfileType(CONSUMPTION).build();
    private String name;
    /*
     * Workload profile type for the workloads to run on.
     */
    private String workloadProfileType;

    /*
     * The minimum capacity.
     */
    private Integer minimumCount;

    /*
     * The maximum capacity.
     */
    private Integer maximumCount;

    public static com.azure.resourcemanager.appcontainers.models.WorkloadProfile toWorkloadProfile(WorkloadProfile workloadProfile) {
        return new com.azure.resourcemanager.appcontainers.models.WorkloadProfile()
            .withName(workloadProfile.getName())
            .withWorkloadProfileType(workloadProfile.getWorkloadProfileType())
            .withMinimumCount(workloadProfile.getMinimumCount())
            .withMaximumCount(workloadProfile.getMaximumCount());
    }

    public static WorkloadProfile fromProfile(com.azure.resourcemanager.appcontainers.models.WorkloadProfile workloadProfile) {
        if (StringUtils.equalsIgnoreCase(workloadProfile.name(), CONSUMPTION)) {
            return CONSUMPTION_PROFILE;
        }
        return WorkloadProfile.builder().name(workloadProfile.name())
            .workloadProfileType(workloadProfile.workloadProfileType())
            .maximumCount(workloadProfile.maximumCount())
            .minimumCount(workloadProfile.minimumCount()).build();
    }
}
