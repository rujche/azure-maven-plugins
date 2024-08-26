package com.microsoft.azure.maven.container.apps.implementation;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PomProjectAnalyzerTest {

    @Test
    public void containsDependencyTest() throws XmlPullParserException, IOException {
        PomProjectAnalyzer analyzer = new PomProjectAnalyzer("src/test/resources/pom-project-analyzer/pom.xml");
        assertFalse(analyzer.containsDependency("not-exist", "not-exist"));
        assertTrue(analyzer.containsDependency("org.springframework.boot", "spring-boot-starter-data-mongodb"));
    }

}
