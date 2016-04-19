package org.openntf.eclipse.xdpe.publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;

public class JavaCodeEntry {
	private final JarEntry javaCode;
	private final List<JarEntry> classes = new ArrayList<JarEntry>();

	public JavaCodeEntry(JarEntry xpages) {
		this.javaCode = xpages;
	}

	public void addClass(JarEntry classEntry) {
		classes.add(classEntry);
	}

	public JarEntry getJavaCode() {
		return javaCode;
	}

	public List<JarEntry> getClasses() {
		return classes;
	}

	public String getJavaCodeFileName() {
		return javaCode.getName().substring("Code/Java/".length());
	}

	public List<String> getClassPathElements() {
		List<String> pathList = new ArrayList<String>();
		for (JarEntry je : classes) {
			pathList.add(je.getName().replace("WebContent/", ""));
		}
		return pathList;
	}
}
