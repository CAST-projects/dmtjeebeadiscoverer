package com.castsoftware.dmt.discoverer.jee.bea;

import org.junit.Test;

import com.castsoftware.dmt.engine.discovery.IProjectsDiscoverer;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper.ProfileOrProjectTypeConfiguration;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper.ProfileOrProjectTypeConfiguration.LanguageConfiguration;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscoveryEngineTester;
import com.castsoftware.dmt.engine.project.Profile.ReferenceCollation;

/**
 * Tests for source files based projects discovery
 *
 */
public class JeeBeaProjectsDiscovererUnitTest
{

    private static class JeeBeaProjectsDiscovererTester extends ProjectsDiscoveryEngineTester
    {
        JeeBeaProjectsDiscovererTester(String desc)
        {
            super(JeeBeaProjectsDiscovererUnitTest.class, desc);
        }

        @Override
        protected IProjectsDiscoverer createTestDiscoverer()
        {
            return new JeeBeaProjectsDiscoverer();
        }

        @Override
        protected void configureTestdiscoverer(ProjectsDiscovererWrapper discovererWrapper)
        {
            ProfileOrProjectTypeConfiguration projectTypeConfiguration = discovererWrapper.addProjectTypeConfiguration(
                "dmtdevjeetechno.J2EEProject", "project.xml", ReferenceCollation.WindowsNTFS, "genfiles.properties");
            LanguageConfiguration javaLanguage = projectTypeConfiguration.addLanguageConfiguration("JavaLanguage",
                "*.java;*.sqlj", ReferenceCollation.WindowsNTFS);
            javaLanguage.addResourceTypeConfiguration("JavaContainerLanguage", null, ReferenceCollation.WindowsNTFS, "*.jar",
                ReferenceCollation.WindowsNTFS);
            javaLanguage.addResourceTypeConfiguration("XMLLanguage", "*.xml", ReferenceCollation.WindowsNTFS, null,
                ReferenceCollation.WindowsNTFS);
            javaLanguage.addResourceTypeConfiguration("JavaPropertiesLanguage", "*.properties", ReferenceCollation.WindowsNTFS,
                null,
                ReferenceCollation.WindowsNTFS);
            LanguageConfiguration javaWebServerLanguage = projectTypeConfiguration.addLanguageConfiguration(
                "JavaWebServerLanguage", "*.jsp", ReferenceCollation.WindowsNTFS);
            javaWebServerLanguage.addResourceTypeConfiguration("JavaWebContainerLanguage", null, ReferenceCollation.WindowsNTFS,
                "*.ear;*.war",
                ReferenceCollation.WindowsNTFS);
            projectTypeConfiguration.addLanguageConfiguration("JavaWebClientLanguage",
                "*.htm;*.html;*.htc;*.js;*.vbs", ReferenceCollation.WindowsNTFS);

            // projectOrigin
            discovererWrapper.configure("Bea Java project");
        }
    }

    /**
     * Test discovery of 1 Project
     *
     * @throws Throwable
     *             if anything goes wrong
     */
    @Test
    public void unitTest1() throws Throwable
    {
        new JeeBeaProjectsDiscovererTester("Test1").go();
    }
}
