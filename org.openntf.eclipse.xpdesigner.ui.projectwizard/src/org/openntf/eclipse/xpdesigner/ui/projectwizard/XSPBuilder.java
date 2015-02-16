package org.openntf.eclipse.xpdesigner.ui.projectwizard;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public enum XSPBuilder {
	INSTANCE;

	public IFile createFile(IProject project, String target, String xpageName) throws CoreException, IOException {
		IFile newFile = project.getFile(target + "/" + xpageName + ".xsp");
		ByteArrayInputStream stream = new ByteArrayInputStream(getXMLSource().getBytes(project.getDefaultCharset()));
		if (newFile.exists())
			newFile.setContents(stream, false, true, null);
		else
			newFile.create(stream, false, null);
		stream.close();
		return newFile;
	}

	public String getXMLSource() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<xp:view xmlns:xp=\"http://www.ibm.com/xsp/core\">\n");
		sb.append("\n");
		sb.append("</xp:view>");
		return sb.toString();
	}
}
