package com.castsoftware.dmt.discoverer.jee.bea;

import java.util.Set;

import com.castsoftware.dmt.engine.discovery.BasicProjectsDiscovererAdapter;
import com.castsoftware.dmt.engine.discovery.IProjectsDiscovererUtilities;
import com.castsoftware.dmt.engine.project.IProfileReadOnly;
import com.castsoftware.dmt.engine.project.Project;
import com.castsoftware.util.logger.Logging;

/**
 * Basic discoverer for
 */
public class JeeBeaProjectsDiscoverer extends BasicProjectsDiscovererAdapter
{
    /**
     * Default constructor used by the discovery engine
     */
    public JeeBeaProjectsDiscoverer()
    {
    }

    @Override
    public void buildProject(String relativeFilePath, String content, Project project,
        IProjectsDiscovererUtilities projectsDiscovererUtilities)
    {
    	Logging.info("cast.dmt.discover.jee.bea.startbuildProject","FILE",relativeFilePath);
        String projectDescriptor = project.getMetadata(IProfileReadOnly.METADATA_DESCRIPTOR).getValue();
        if ((!projectDescriptor.endsWith(".work")) || (!parseProjectFile(project, content, projectsDiscovererUtilities)))
            projectsDiscovererUtilities.deleteProject(project.getId());
    	Logging.info("cast.dmt.discover.jee.bea.endbuildProject","FILE",relativeFilePath);
    }

    private static boolean parseProjectFile(Project project, String content, IProjectsDiscovererUtilities projectsDiscovererUtilities)
    {
        ProjectFileScanner.scan(project, content, projectsDiscovererUtilities);
        // always false to drop the project created by default
        return false;
    }
}
