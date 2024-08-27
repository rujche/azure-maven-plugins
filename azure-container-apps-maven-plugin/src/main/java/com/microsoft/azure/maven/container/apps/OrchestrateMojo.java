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
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

@Mojo(name = "orchestrate")
public class OrchestrateMojo extends CreateProjectFromArchetypeMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        generateAppHost();
        generateAzureYml();
        callAzdInit();
    }

    private void generateAppHost() throws MojoExecutionException, MojoFailureException {
        getLog().info("Generating App Host.");
        setParentFields();
        updateUserProperties();
        super.execute();
    }

    private void generateAzureYml() {
        getLog().info("Generating azure.yml.");
        try {
            Files.copy(
                Objects.requireNonNull(OrchestrateMojo.class.getResourceAsStream("/azure-container-apps-maven-plugin/azure.yaml")),
                new File("./azure.yaml").toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void callAzdInit() {
        getLog().info("Calling 'azd-init'.");
        try {
            Runtime.getRuntime().exec("azd init");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            // 创建 ProcessBuilder 来启动 azd init 命令
            ProcessBuilder builder = new ProcessBuilder("azd", "init");
            Process process = builder.start();

            // 获取进程的输入流和输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            OutputStream outputStream = process.getOutputStream();

            // 创建一个新线程来读取进程的输出
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

            // 使用 Scanner 来读取用户输入
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                outputStream.write((input + "\n").getBytes());
                outputStream.flush();
            }

            // 等待进程结束
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
