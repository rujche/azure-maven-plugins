package com.microsoft.azure.maven.container.apps;

import org.apache.maven.archetype.mojos.CreateProjectFromArchetypeMojo;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.lang.reflect.Field;

@Mojo(name = "orchestrate")
public class OrchestrateMojo extends CreateProjectFromArchetypeMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("Orchestrate Mojo started");
        updateSuperFields();
        super.execute();
    }

    private void updateSuperFields() {
        setParentPrivateField("archetypeGroupId", "com.azure");
        setParentPrivateField("archetypeArtifactId", "azure-runtime-maven-tools");
        setParentPrivateField("archetypeVersion", "1.0-SNAPSHOT");
        setParentPrivateField("interactiveMode", false);
        updateUserProperties();
    }

    private void updateUserProperties() {
        try {
            Field field = CreateProjectFromArchetypeMojo.class.getDeclaredField("session");
            field.setAccessible(true);
            MavenSession session = (MavenSession) field.get(this);
            session.getUserProperties().setProperty("groupId", "com.microsoft.azure.container.apps.maven.plugin.generated");
            session.getUserProperties().setProperty("artifactId", "app-host");
            session.getUserProperties().setProperty("version", "1.0.0-SNAPSHOT");
            session.getUserProperties().setProperty("package", "jar");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setParentPrivateField(String fieldName, Object value) {
        Field field = null;
        try {
            field = CreateProjectFromArchetypeMojo.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
