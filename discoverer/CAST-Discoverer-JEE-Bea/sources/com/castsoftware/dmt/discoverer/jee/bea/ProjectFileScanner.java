package com.castsoftware.dmt.discoverer.jee.bea;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.Attributes;

import com.castsoftware.dmt.engine.discovery.IProjectsDiscovererUtilities;
import com.castsoftware.dmt.engine.discovery.ProjectsDiscovererWrapper.ProfileOrProjectTypeConfiguration.LanguageConfiguration;
import com.castsoftware.dmt.engine.project.Profile;
import com.castsoftware.dmt.engine.project.Project;
import com.castsoftware.util.StringHelper;
import com.castsoftware.util.logger.Logging;
import com.castsoftware.util.xml.AbstractXMLFileReader;
import com.castsoftware.util.xml.IInterpreter;

/**
 * Scanner for bea .work files.
 */
public class ProjectFileScanner
{
    protected static final String META_JSP_WEB_ROOT = "applicationRootPath";
    protected static final String META_JSP_WEB_APPDESCRIPTOR = "applicationDescriptor";
    /**
     * Interpreter of a bea .work file
     */
    public interface IProjectInterpreter extends IInterpreter
    {

        /**
         * Add a project
         *
         * @param name
         *            the name of the project
         */
        void addProject(String name);

        /**
         * Reset the project
         *
         */
        void resetProject();

        /**
         * Interpret the type of the project
         *
         * @param type
         *            the type of the project
         */
        void setProjectType(String type);

        /**
         * Set the project path
         *
         * @param path
         *            the path of the project
         */
        void setProjectPath(String path);

        /**
         * Adding the source folders to the project
         *
         */
        void addProjectSourceFolders();

        /**
         * Adding a classpath defined in a compilation unit to the list
         *
         * @param classpath
         *            the added classpath (a jar file)
         */
        void addClasspath(String classpath);

        /**
         * Adding the classpaths to the project
         *
         */
        void addClasspaths();

        /**
         * Adding the dependencies between projects
         *
         */
        void addProjectDependencies();
    }

    private static class ProjectRecorder implements IProjectInterpreter
    {
    	private final IProjectsDiscovererUtilities projectsDiscovererUtilities;
        private final Project project;
        private final int javaLanguageId;
        private final int javaContainerLanguageId;
        private final int  javaWebServerLanguage;
        private final int javaWebClientLanguage;

        private Project currentProject;
        private final Set<String> classpaths;
        private final Set<String> sourcepaths;
        private String projectPath;
        private String projectType;
        private final Set<String> javaProjects;
        private final Set<String> webProjects;
        private final Set<String> ejbProjects;

        private ProjectRecorder(IProjectsDiscovererUtilities projectsDiscovererUtilities, Project project, int javaLanguageId, int javaContainerLanguageId, int javaWebServerLanguage, int javaWebClientLanguage)
        {
        	this.projectsDiscovererUtilities = projectsDiscovererUtilities;
        	this.project = project;
            this.javaLanguageId = javaLanguageId;
            this.javaContainerLanguageId = javaContainerLanguageId;
            this.javaWebServerLanguage = javaWebServerLanguage;
            this.javaWebClientLanguage = javaWebClientLanguage;

        	currentProject = null;
        	classpaths = new HashSet<String>();
        	sourcepaths = new HashSet<String>();
            javaProjects = new HashSet<String>();
            webProjects = new HashSet<String>();
            ejbProjects = new HashSet<String>();
        }

        @Override
        public void init()
        {
            // ignore
        }

        @Override
        public void done()
        {
            // ignore
        }

        @Override
        public void open(String resourceId)
        {
            // ignore
        }

        @Override
        public void close()
        {
            // ignore
        }

		@Override
        public void addProject(String name)
        {
			if (!"urn:com-bea-ide:project.type:Schema".equals(projectType))
			{
				String id = project.getId().concat("_").concat(name);
				String resourceId = project.getResourceId().concat("_").concat(name);
				currentProject = projectsDiscovererUtilities.createInitialProject(id, name, project.getType(), resourceId, projectPath);
	            currentProject.addMetadata("beaType", projectType);
			}
			return;
		}

		@Override
        public void resetProject()
        {
			//currentProject = null;
            classpaths.clear();
            sourcepaths.clear();
            projectPath = null;
            projectType = null;
            return;
		}
        @Override
        public void addProjectSourceFolders()
        {
            if ("urn:com-bea-ide:project.type:Java".equals(projectType))
            {
                javaProjects.add(currentProject.getId());

                //String projectFolder = projectPath;
                //if (projectFolder.startsWith("./"))
                //	projectFolder = buildPackageRelativePath(currentProject, projectFolder.substring(2));

                if (sourcepaths.size() == 0)
                	//currentProject.addSourceDirectoryReference(projectFolder, javaLanguageId);
                	currentProject.addSourceDirectoryReference(projectPath, javaLanguageId);
                else
                {
                	for (String sourcepath : sourcepaths)
                	{
                		//String sourceFolder = projectFolder.concat("/").concat(sourcepath);
                		String sourceFolder = projectPath.concat("/").concat(sourcepath);
                		currentProject.addSourceDirectoryReference(sourceFolder, javaLanguageId);
                	}
                }
            }
            else if ("urn:com-bea-ide:project.type:WebApp".equals(projectType))
            {
                webProjects.add(currentProject.getId());
                //String sourceFolder = projectPath;
                //if (sourceFolder.startsWith("./"))
                //    sourceFolder = buildPackageRelativePath(currentProject, sourceFolder.substring(2));
                //currentProject.addMetadata(META_JSP_WEB_APPDESCRIPTOR, sourceFolder.concat("/WEB-INF/web.xml"));
                currentProject.addMetadata(META_JSP_WEB_APPDESCRIPTOR, projectPath.concat("/WEB-INF/web.xml"));
                //currentProject.addMetadata(META_JSP_WEB_ROOT, sourceFolder);
                currentProject.addMetadata(META_JSP_WEB_ROOT, projectPath);
                //currentProject.addSourceDirectoryReference(sourceFolder, javaWebServerLanguage);
                currentProject.addSourceDirectoryReference(projectPath, javaWebServerLanguage);
                //currentProject.addSourceDirectoryReference(sourceFolder, javaWebClientLanguage);
                currentProject.addSourceDirectoryReference(projectPath, javaWebClientLanguage);
            }
            else if ("urn:com-bea-ide:project.type:Schema".equals(projectType))
            {
            	// skip Schemas
            }
            else if ("urn:com-bea-ide:project.type:EJB".equals(projectType))
            {
                ejbProjects.add(currentProject.getId());

                //String projectFolder = projectPath;
                //if (projectFolder.startsWith("./"))
                //	projectFolder = buildPackageRelativePath(currentProject, projectFolder.substring(2));

                if (sourcepaths.size() == 0)
                	//currentProject.addSourceDirectoryReference(projectFolder, javaLanguageId);
                	currentProject.addSourceDirectoryReference(projectPath, javaLanguageId);
                else
                {
                	for (String sourcepath : sourcepaths)
                	{
                		//String sourceFolder = projectFolder.concat("/").concat(sourcepath);
                		String sourceFolder = projectPath.concat("/").concat(sourcepath);
                		currentProject.addSourceDirectoryReference(sourceFolder, javaLanguageId);
                	}
                }
            }

        }

		@Override
		public void addClasspath(String classpath) {
            List<String> list = new ArrayList<String>(Arrays.asList(classpath.split(";")));
            for (int i = 0; i < list.size(); i++)
            {
                String path = list.get(i);
                if (!StringHelper.isEmpty(path))
                    classpaths.add(path);
            }
		}

		@Override
		public void addClasspaths() {
            for (String path : classpaths)
            {
                int varStart = path.indexOf("${");
                if (varStart >= 0)
                {
                    int varEnd = path.indexOf("}", varStart);
                    if (varEnd > 0)
                    {
                        String name = path.substring(varStart + 2, varEnd);
                        if (name.endsWith(".root") && name.substring(0, name.length() - 5).equals(currentProject.getName()))
                        {
                            if (varStart > 0)
                                path = path.substring(0, varStart - 1).concat(projectPath)
                                        .concat(path.substring(varEnd + 1));
                            else
                                path = projectPath.concat(path.substring(varEnd + 1));
                        }
                        else
                        {
                            // not supported
                            continue;
                        }
                    }
                }
                String classpath = path;
                if (path.startsWith("./"))
                    classpath = buildPackageRelativePath(currentProject, path.substring(2));
                if (classpath.toLowerCase().endsWith(".jar"))
                {
                    currentProject.addContainerReference(classpath, javaLanguageId, javaContainerLanguageId);
                }
                else
                {
                    // do not add the classpath if it's already the sources
                    if (!projectPath.contains(classpath))
                    {
                        currentProject.addDirectoryReference(classpath, javaLanguageId, javaContainerLanguageId);
                    }
                }
            }
            classpaths.clear();
            // add implicit classpath
            // TODO: extract the option "output.directory" from the node "EARBuild"
            // <component name="com.bea.ide.workspace.IWorkspace">
            // <node name="EARBuild">
            // <option name="output.directory" value="." />
            String libpath = project.getPath().concat("/APP-INF/lib");
            currentProject.addDirectoryReference(libpath, javaLanguageId, javaContainerLanguageId);
		}

		@Override
		public void setProjectType(String type) {
            projectType = type;
		}

        @Override
        public void setProjectPath(String path)
        {
            projectPath = project.getPath().concat(path.substring(1));
        }

        @Override
        public void addProjectDependencies()
        {
        	for (String webProject : webProjects)
        	{
	            Project p = projectsDiscovererUtilities.getProject(webProject);
	            for (String id : javaProjects)
	            {
	                p.addProjectReference(id);
	                Project j = projectsDiscovererUtilities.getProject(id);
	                for (String dep : javaProjects)
	                {
	                    if (!dep.equals(id))
	                        j.addProjectReference(dep);
	                }
	            }
        	}

        	for (String ejbProject : ejbProjects)
        	{
	            Project p = projectsDiscovererUtilities.getProject(ejbProject);
	            for (String id : javaProjects)
	            {
	                p.addProjectReference(id);
	                Project j = projectsDiscovererUtilities.getProject(id);
	                for (String dep : javaProjects)
	                {
	                    if (!dep.equals(id))
	                        j.addProjectReference(dep);
	                }
	            }
        	}
        }
    }

    private static class BeaProjectReader extends AbstractXMLFileReader
    {
        private IProjectInterpreter interpreter;

        private boolean isInApplication;
        private boolean isInComponents;
        private boolean isInComponent;
        private String componentName;
        private boolean isInProjects;
        private boolean isInProject;
        private int depth;
        private String projectName;
        private String projectType;

        private BeaProjectReader()
        {
            // NOP
        }

        private boolean process(IProjectInterpreter projectInterpreter, String filePath, String content)
        {
            interpreter = projectInterpreter;

            isInApplication = false;
            isInComponents = false;
            isInComponent = false;
            componentName = null;
            isInProjects = false;
            isInProject = false;
            depth = 0;


            StringReader reader = new StringReader(content);
            boolean isOk = readContents(interpreter, filePath, reader, false);

            interpreter = null;

            return isOk;
        }

        @Override
        protected void startElement(String elementName, Attributes attributes)
        {
        	if ("application".equals(elementName))
        		isInApplication = true;
            else if (isInApplication)
            {
            	depth++;
            	if ("option".equals(elementName))
            	{
            		if (isInProject)
                    {
                        String optionName = attributes.getValue("name");
                        if (depth == 3)
                        {
                            if ("project.path".equals(optionName))
                                // interpreter.addSourceFolder(attributes.getValue("value"));
                                interpreter.setProjectPath(attributes.getValue("value"));
                            else if ("type".equals(optionName))
                            {
                                projectType = attributes.getValue("value");
                                interpreter.setProjectType(projectType);
                            }
                        }
                        else if (isInComponents && isInComponent && depth == 5)
                        {
                            if ("com.bea.ide.JavaCompiler".equals(componentName))
                            {
                                if ("class.path".equals(optionName))
                                {
                                    interpreter.addClasspath(attributes.getValue("value"));
                                }
                                else if ("source.path".equals(optionName))
                                {
                                    // TODO: always empty
                                }
                            }
                        }
                    }
            	}
            	else if ("projects".equals(elementName))
            	{
            		isInProjects = true;
            	}
            	else if ("project".equals(elementName))
            	{
            		if (isInProjects)
            		{
            			isInProject = true;
            			projectName = attributes.getValue("name");
            		}
            	}
                else if ("components".equals(elementName))
                    isInComponents = true;
                else if ("component".equals(elementName))
                {
                	// inside a project
                	// the component is either empty
                	// <component name="com.bea.ide.JavaCompiler" />
                	//
                	//
                	// <component name="com.bea.ide.JavaCompiler">
                	//   <option name="class.path" value="" />
                	//   <option name="source.path" value="" />
                	// </component>
                    isInComponent = true;
                    componentName = attributes.getValue("name");
                }
            }
        }

        @Override
        protected void endElement(String elementName)
        {
            if (isInApplication && depth > 0)
            {
                depth--;
            }
            if ("project".equals(elementName))
            {
                if (isInProject)
                {
            		isInProject = false;
                    if (!"urn:com-bea-ide:project.type:Schema".equals(projectType))
                    {
                        interpreter.addProject(projectName);
                        interpreter.addProjectSourceFolders();
                        interpreter.addClasspaths();
                    }
                    interpreter.resetProject();
                    projectName = null;
                }
            }
            else if ("projects".equals(elementName))
            {
                if (isInProjects)
                {
            		isInProjects = false;
                    interpreter.addProjectDependencies();
                }
            }
            else if ("component".equals(elementName))
            {
                if (isInComponent)
                {
                    isInComponent = false;
                    componentName = "";
                }
            }
            else if ("components".equals(elementName))
            {
                if (isInComponents)
                    isInComponents = false;
            }
        }

    }

    private ProjectFileScanner()
    {
        // NOP
    }

    /**
     * Scan a .project file and add info to the project.
     *
     * @param interpreter
     *            the project file interpreter
     * @param projectFilePath
     *            the path to the project file used for reference
     * @param projectContent
     *            the file content to scan.
     * @return {@code true} if no error was encountered during scanning. {@code false} otherwise.
     */
    public static boolean scan(IProjectInterpreter interpreter, String projectFilePath, String projectContent)
    {
        BeaProjectReader reader = new BeaProjectReader();

        return reader.process(interpreter, projectFilePath, projectContent);
    }


    private static String buildPackageRelativePath(Project project, String projectPath)
    {
        if (projectPath.startsWith("/"))
        {
            int slashPos = projectPath.indexOf('/', 1);
            if (slashPos == -1)
                return projectPath.substring(1);

            String projectRelativePath = projectPath.substring(slashPos + 1);
            String projectName = projectPath.substring(1, slashPos);

            return Profile.buildPackageRelativePath(projectName, projectRelativePath);
        }

        if (new File(projectPath).isAbsolute())
            return projectPath;

        return project.buildPackageRelativePath(projectPath);
    }

    /**
     * Scan a .project file, add info to the project and return the project natures.
     *
     * @param project
     *            the project containing this file
     * @param projectContent
     *            the file content to scan.
     * @param javaLanguageId
     *            the java language ID to use to reference java files and folders.
     * @param javaContainerLanguageId
     *            the java container language ID to use to reference jar files or classpath.
     * @return null if an error was encountered during scanning. Otherwise a set containing the project natures.
     */
    public static void scan(Project project, String projectContent, IProjectsDiscovererUtilities projectsDiscovererUtilities)
    {
    	int javaLanguageId = -1;
    	int javaContainerLanguageId = -1;
    	int javaWebServerLanguage = -1;
    	int javaWebClientLanguage = -1;

        for (LanguageConfiguration languageConfiguration : projectsDiscovererUtilities.getProjectTypeConfiguration(project.getType()).getLanguageConfigurations())
        {
            int languageId = languageConfiguration.getLanguageId();
            if ("JavaLanguage".equals(languageConfiguration.getLanguageName()))
            {
                // TODO: not available in 7.3.x API => hardcoded value
            	javaLanguageId = languageId;
            	/*
                if ("JavaContainerLanguage".equals(languageConfiguration.getLanguageName()))
                	javaContainerLanguageId = languageId;
                */
                if (javaContainerLanguageId == -1)
                {
                	javaContainerLanguageId = 1;
                    //Logging.managedError("cast.dmt.discover.jee.bea.getJavaContainerLanguageFailure");
                }
            }
            else if ("JavaWebServerLanguage".equals(languageConfiguration.getLanguageName()))
            {
                // TODO: not available in 7.3.x API => hardcoded value
            	javaWebServerLanguage = languageId;
            }
            else if ("JavaWebClientLanguage".equals(languageConfiguration.getLanguageName()))
            {
                // TODO: not available in 7.3.x API => hardcoded value
            	javaWebClientLanguage = languageId;
            }
        }
        if (javaLanguageId == -1)
        {
            Logging.managedError("cast.dmt.discover.jee.bea.getJavaLanguageFailure");
        }

        IProjectInterpreter interpreter = new ProjectRecorder(projectsDiscovererUtilities, project, javaLanguageId, javaContainerLanguageId, javaWebServerLanguage, javaWebClientLanguage);
        scan(interpreter, project.getPath(), projectContent);
        return;
    }

}
