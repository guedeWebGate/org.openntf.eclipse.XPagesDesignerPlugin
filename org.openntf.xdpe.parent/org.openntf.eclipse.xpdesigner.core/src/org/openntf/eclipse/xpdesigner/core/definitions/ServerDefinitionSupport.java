package org.openntf.eclipse.xpdesigner.core.definitions;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public enum ServerDefinitionSupport {
	INSTANCE;

	public boolean initServerDefinition(String serverName, String serverFQDN, String port) {
		IProject xpageServerProject = ServerDefinitionUtils.getXPagesServerProject(true);
		if (xpageServerProject == null) {
			return false;
		}
		if (ServerDefinitionUtils.folderExist(xpageServerProject, serverName, true)) {
			return false;
		}
		IFile file = xpageServerProject.getFile(serverName + "/server.xde-server-properties");
		Properties props = new Properties();
		props.setProperty("SERVER_NAME", serverName);
		props.setProperty("SERVER_FQDN", serverFQDN);
		props.setProperty("SERVER_PORT", port);
		try {
			ServerDefinitionUtils.saveProperties2File(props, file);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return true;
	}

	public IProject getXPagesServerProject(boolean createIfNotExist) {

		IProject xpageServerProject = ResourcesPlugin.getWorkspace().getRoot().getProject("XPagesServers");

		if (!xpageServerProject.exists() && !createIfNotExist) {
			return null;
		}

		if (!xpageServerProject.exists() && createIfNotExist) {
			IProjectDescription desc = xpageServerProject.getWorkspace().newProjectDescription(xpageServerProject.getName());
			desc.setLocationURI(null);
			try {
				xpageServerProject.create(desc, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		try {
			if (!xpageServerProject.isOpen()) {
				xpageServerProject.open(null);
				return xpageServerProject;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return xpageServerProject;
	}

	public boolean checkHasServerName(String name) {
		IProject project = getXPagesServerProject(false);
		if (project == null) {
			return false;
		}
		return ServerDefinitionUtils.folderExist(project, name, false);
	}

	public List<String> getServerDefinitionNames() {
		List<String> serverDefinitionNames = new LinkedList<String>();
		IProject project = getXPagesServerProject(false);
		if (project != null) {
			try {
				for (IResource resource : project.members()) {
					if (resource instanceof IFolder && ((IFolder) resource).getParent() instanceof IProject) {
						serverDefinitionNames.add(resource.getName());
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return serverDefinitionNames;
	}
}
