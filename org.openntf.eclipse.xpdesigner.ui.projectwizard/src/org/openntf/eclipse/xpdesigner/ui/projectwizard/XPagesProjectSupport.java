package org.openntf.eclipse.xpdesigner.ui.projectwizard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.build.IBuildModelFactory;
import org.eclipse.pde.core.plugin.IPlugin;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginBase;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDELibrary;
import org.openntf.eclipse.xpdesigner.ui.natures.ProjectNature;
import org.osgi.framework.Constants;

@SuppressWarnings("restriction")
public class XPagesProjectSupport {
	/**
	 * For this marvelous project we need to: - create the default Eclipse
	 * project - add the custom project nature - create the folder structure
	 *
	 * @param projectName
	 * @param location
	 * @param natureId
	 * @return
	 * @throws IOException
	 */
	public static IProject createProject(String projectName, URI location, List<XDELibrary> libs) {
		Assert.isNotNull(projectName);
		Assert.isTrue(projectName.trim().length() > 0);

		IProject project = createBaseProject(projectName, location);
		try {
			addNature(project);

			String[] paths = { "Code/Java", "Forms", "Views", "CustomControls", "XPages", "WebContent/META-INF/classes", "WebContent/js", "WebContent/css", "Generated/xsp", "Generated/plugin" }; //$NON-NLS-1$ //$NON-NLS-2$
			addToProjectStructure(project, paths);

			setClasspath(project, "WebContent/META-INF/classes");
			WorkspaceBundlePluginModel model = initPluginStructure(projectName, project);
			addImports(model);
			addSelectedExtensionLibraries(model, libs);
			createActivator(project);
			createBuildProperties(project);
			model.save();
		} catch (CoreException e) {
			e.printStackTrace();
			project = null;
		} catch (IOException e) {
			e.printStackTrace();
			project = null;
		}

		return project;
	}

	private static void addSelectedExtensionLibraries(WorkspaceBundlePluginModel model, List<XDELibrary> libs) throws CoreException {
		IPluginBase base = model.getPluginBase();
		IPluginImport[] imports = base.getImports();
		List<IPluginImport> toImport = PluginDependencyManager.INSTANCE.buildImportsDiff(libs, imports, model);
		for (IPluginImport imp : toImport) {
			base.add(imp);
		}

	}

	private static void addImports(WorkspaceBundlePluginModel model) throws CoreException {
		List<IPluginImport> plugs = PluginDependencyManager.INSTANCE.buildMandatoryImports(model);
		IPluginBase base = model.getPluginBase();
		for (IPluginImport imp : plugs) {
			base.add(imp);
		}
	}

	@SuppressWarnings("deprecation")
	private static WorkspaceBundlePluginModel initPluginStructure(String projectName, IProject project) throws CoreException {
		IFile pluginXml = project.getFile("plugin.xml");
		IFile manifest = project.getFile("META-INF/MANIFEST.MF");
		WorkspaceBundlePluginModel model = new WorkspaceBundlePluginModel(manifest, pluginXml);
		IPluginBase pluginBase = model.getPluginBase();
		String targVersion = TargetPlatformHelper.getTargetVersionString();
		pluginBase.setSchemaVersion(TargetPlatformHelper.getSchemaVersionForTargetVersion(targVersion));
		pluginBase.setId(project.getName());
		pluginBase.setName(projectName);
		pluginBase.setVersion("1.0.0.qualifier");

		model.getBundleModel().getBundle().setHeader(Constants.BUNDLE_MANIFESTVERSION, "2");
		IPlugin plug = (IPlugin) pluginBase;
		plug.setClassName("plugin.Activator");

		IBundlePluginBase plugBundle = ((IBundlePluginBase) pluginBase);
		plugBundle.setTargetVersion(targVersion);
		model.getBundleModel().getBundle().setHeader(Constants.BUNDLE_ACTIVATIONPOLICY, Constants.ACTIVATION_LAZY);
		model.getBundleModel().getBundle().setHeader(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT, "JavaSE-1.6");

		return model;
	}

	private static void createActivator(IProject project) throws IOException, CoreException {
		IFile activatorFile = project.getFile("Generated/plugin/Activator.java");
		InputStream in = Activator.getDefault().getClass().getResourceAsStream("/resources/Activator.txt");
		StringWriter writer = new StringWriter();
		IOUtils.copy(in, writer, project.getDefaultCharset());
		String code = writer.toString();
		code = code.replace("###PLUGIN_ID###", project.getName());
		ByteArrayInputStream stream = new ByteArrayInputStream(code.getBytes(project.getDefaultCharset()));
		if (activatorFile.exists())
			activatorFile.setContents(stream, false, true, null);
		else
			activatorFile.create(stream, false, null);
		stream.close();

	}

	private static void createBuildProperties(IProject project) throws CoreException {
		IFile file = project.getFile("build.properties");
		if (!file.exists()) {
			WorkspaceBuildModel model = new WorkspaceBuildModel(file);
			IBuildModelFactory factory = model.getFactory();

			// SOURCE..
			IBuildEntry source = factory.createEntry("source..");
			source.addToken("Code/Java");
			source.addToken("Generated");
			model.getBuild().add(source);

			// BUILD..
			IBuildEntry bin = factory.createEntry("output..");
			bin.addToken("WebContent/META-INF/classes");
			model.getBuild().add(bin);

			// BIN.INCLUDES
			IBuildEntry binIncludesEntry = factory.createEntry(IBuildEntry.BIN_INCLUDES);
			binIncludesEntry.addToken("META-INF/"); //$NON-NLS-1$
			binIncludesEntry.addToken("."); //$NON-NLS-1$

			model.getBuild().add(binIncludesEntry);
			model.save();
		}
	}

	/**
	 * Just do the basics: create a basic project.
	 *
	 * @param location
	 * @param projectName
	 */
	private static IProject createBaseProject(String projectName, URI location) {
		// it is acceptable to use the ResourcesPlugin class
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		if (!newProject.exists()) {
			URI projectLocation = location;
			IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
			if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
				projectLocation = null;
			}

			desc.setLocationURI(projectLocation);
			try {
				newProject.create(desc, null);
				if (!newProject.isOpen()) {
					newProject.open(null);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		return newProject;
	}

	private static void createFolder(IFolder folder) throws CoreException {
		IContainer parent = folder.getParent();
		if (parent instanceof IFolder) {
			createFolder((IFolder) parent);
		}
		if (!folder.exists()) {
			folder.create(false, true, null);
		}
	}

	/**
	 * Create a folder structure with a parent root, overlay, and a few child
	 * folders.
	 *
	 * @param newProject
	 * @param paths
	 * @throws CoreException
	 */
	private static void addToProjectStructure(IProject newProject, String[] paths) throws CoreException {
		for (String path : paths) {
			IFolder etcFolders = newProject.getFolder(path);
			createFolder(etcFolders);
		}
	}

	private static void addNature(IProject project) throws CoreException {
		if (!project.hasNature(ProjectNature.NATURE_ID)) {
			addNatureToProject(project, ProjectNature.NATURE_ID);
		}
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			addNatureToProject(project, JavaCore.NATURE_ID);
		}
		if (!project.hasNature("org.eclipse.pde.PluginNature")) {
			addNatureToProject(project, "org.eclipse.pde.PluginNature");
		}

		if (!project.hasNature("org.eclipse.wst.jsdt.core.jsNature")) {
			addNatureToProject(project, "org.eclipse.wst.jsdt.core.jsNature");
		}
	}

	private static void addNatureToProject(IProject project, String nature) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = nature;
		description.setNatureIds(newNatures);

		IProgressMonitor monitor = null;
		project.setDescription(description, monitor);
	}

	private static void setClasspath(IProject project, String outputFolder) throws JavaModelException, CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		IPath path = project.getFullPath().append(outputFolder);
		javaProject.setOutputLocation(path, null);
		List<IClasspathEntry> entries = getClassPathEntries(javaProject, project);
		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
	}

	private static List<IClasspathEntry> getClassPathEntries(IJavaProject javaProject, IProject project) {
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		entries.add(createJREEntry("JavaSE-1.6"));
		entries.add(JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins")));
		IPath path = project.getProject().getFullPath();
		entries.add(JavaCore.newSourceEntry(path.append("Code/Java")));
		entries.add(JavaCore.newSourceEntry(path.append("Generated")));
		return entries;
	}

	public static IClasspathEntry createJREEntry(String ee) {
		return JavaCore.newContainerEntry(getEEPath(ee));
	}

	/**
	 * Returns the JRE container path for the execution environment with the
	 * given id.
	 * 
	 * @param ee
	 *            execution environment id or <code>null</code>
	 * @return JRE container path for the execution environment
	 */
	private static IPath getEEPath(String ee) {
		IPath path = null;
		if (ee != null) {
			IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
			IExecutionEnvironment env = manager.getEnvironment(ee);
			if (env != null)
				path = JavaRuntime.newJREContainerPath(env);
		}
		if (path == null) {
			path = JavaRuntime.newDefaultJREContainerPath();
		}
		return path;
	}

	public static Map<String, IProject> buildXPagesProjectList() {
		Map<String, IProject> projectList = new TreeMap<String, IProject>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			try {
				if (project.hasNature(ProjectNature.NATURE_ID)) {
					projectList.put(project.getName(), project);
				}
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}
		return projectList;
	}
	public static List<String> getProjectsAsList(Map<String, IProject> projectList) {
		List<String> list = new LinkedList<String>();
		list.addAll(projectList.keySet());
		return list;
	}
}