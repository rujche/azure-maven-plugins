package com.microsoft.azure.maven.container.apps.implementation;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PomProjectAnalyzer {

    private final Set<String> dependencies;
    private final List<PomProjectAnalyzer> modules;

    public PomProjectAnalyzer(String pomFilePath) throws IOException, XmlPullParserException {
        this.dependencies = new HashSet<>();
        this.modules = new ArrayList<>();
        FileReader reader = new FileReader(pomFilePath);
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        Model model = mavenReader.read(reader);
        MavenProject project = new MavenProject(model);
        for (final Dependency dependency : project.getDependencies()) {
            dependencies.add(createDependencyString(dependency.getGroupId(), dependency.getArtifactId()));
        }
        for (final String module : project.getModules()) {
            modules.add(new PomProjectAnalyzer(String.format("%s/%s/pom.xml",
                pomFilePath.substring(0, pomFilePath.lastIndexOf("/")), module)));
        }
    }

    public boolean containsDependency(String groupId, String artifactId) {
        if (dependencies.contains(createDependencyString(groupId, artifactId))) {
            return true;
        }
        for (final PomProjectAnalyzer module : modules) {
            if (module.containsDependency(groupId, artifactId)) {
                return true;
            }
        }
        return false;
    }

    private String createDependencyString(String groupId, String artifactId) {
        return String.format("%s:%s", groupId, artifactId);
    }

}
