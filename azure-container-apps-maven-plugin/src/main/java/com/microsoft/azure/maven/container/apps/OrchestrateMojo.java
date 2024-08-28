package com.microsoft.azure.maven.container.apps;

import com.microsoft.azure.maven.container.apps.implementation.PomProjectAnalyzer;
import org.apache.maven.archetype.mojos.CreateProjectFromArchetypeMojo;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

@Mojo(name = "orchestrate")
public class OrchestrateMojo extends CreateProjectFromArchetypeMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        generateAppHost();
        generateAzureYml();
        runAzdInit();
        runAzdUp();
    }

    public static void main(String[] args) {
        OrchestrateMojo mojo = new OrchestrateMojo();
        mojo.executeCommand("azd", "up");
    }

    private void generateAppHost() throws MojoExecutionException, MojoFailureException {
        getLog().info("Generating App Host.");
        setParentFields();
        updateUserProperties();
        super.execute();
    }

    private void generateAzureYml() {
        getLog().info("Generating azure.yaml.");
        try {
            Files.copy(
                Objects.requireNonNull(OrchestrateMojo.class.getResourceAsStream("/azure-container-apps-maven-plugin/azure.yaml")),
                new File("./azure.yaml").toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runAzdInit() {
        // executeCommand("azd init"); // Now azd init can not execute successfully
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please run 'azd init' in another Terminal. After 'azd init' finished, press enter here.");
        scanner.nextLine();
    }

    private void runAzdUp() {
        executeCommand("azd", "up");
    }

    private void executeCommand(String... command) {
        getLog().info("Executing command: " + Arrays.toString(command));
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            OutputStream outputStream = process.getOutputStream();

            new Thread(() -> {
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                outputStream.write((input + "\n").getBytes());
                outputStream.flush();
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setParentFields() {
        setParentPrivateField("archetypeGroupId", "com.azure");
        setParentPrivateField("archetypeArtifactId", "azure-runtime-maven-tools");
        setParentPrivateField("archetypeVersion", "1.0-SNAPSHOT");
        setParentPrivateField("interactiveMode", false);
    }

    private void setParentPrivateField(String fieldName, Object value) {
        Field field;
        try {
            field = CreateProjectFromArchetypeMojo.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateUserProperties() {
        try {
            Field field = CreateProjectFromArchetypeMojo.class.getDeclaredField("session");
            field.setAccessible(true);
            MavenSession session = (MavenSession) field.get(this);
            Properties properties = session.getUserProperties();
            setArtifactRelatedProperties(properties);
            setArchetypeRelatedProperties(properties);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setArtifactRelatedProperties(Properties properties) {
        properties.setProperty("groupId", "com.microsoft.azure.container.apps.maven.plugin.generated");
        properties.setProperty("artifactId", "app-host");
        properties.setProperty("version", "1.0.0-SNAPSHOT");
        properties.setProperty("package", "jar");
        properties.setProperty("packageInPathFormat", "com.microsoft.azure.container.apps.maven.plugin.generated.app.host");
    }

    private void setArchetypeRelatedProperties(Properties properties) {
        properties.setProperty("moduleId", "com.microsoft.azure.container.apps.maven.plugin.generated.app.host");
        properties.setProperty("includeAzure", "false");
        properties.setProperty("includeSpring", "false");
//        PomProjectAnalyzer analyzer;
//        try {
//            analyzer = new PomProjectAnalyzer("pom.xml");
//        } catch (IOException| XmlPullParserException e) {
//            throw new RuntimeException(e);
//        }
//        properties.setProperty("includeAzure", String.valueOf(includeAzure(analyzer)));
//        properties.setProperty("includeSpring", String.valueOf(includeSpring(analyzer)));
    }

    private boolean includeAzure(PomProjectAnalyzer analyzer) {
        return analyzer.containsDependency("com.azure", "azure-storage-blob");
    }

    private boolean includeSpring(PomProjectAnalyzer analyzer) {
        return analyzer.containsDependencyWithGroupId("org.springframework.boot");
    }

}
