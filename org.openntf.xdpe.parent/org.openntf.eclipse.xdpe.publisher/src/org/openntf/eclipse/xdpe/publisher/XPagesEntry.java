package org.openntf.eclipse.xdpe.publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;

import com.ibm.xsp.page.compiled.PageToClassNameUtil;

public class XPagesEntry {
	private final JarEntry xpages;
	private final List<JarEntry> classes = new ArrayList<JarEntry>();

	public XPagesEntry(JarEntry xpages) {
		this.xpages = xpages;
	}

	public void addClass(JarEntry classEntry) {
		classes.add(classEntry);
	}

	public JarEntry getXpages() {
		return xpages;
	}

	public List<JarEntry> getClasses() {
		return classes;
	}

	public String getXPagesName() {
		return xpages.getName().replace("XPages", "");
	}

	public String getXPagesClassName() {
		return PageToClassNameUtil.getClassNameForPage(getXPagesName());
	}

	public List<String> getClassPathElements() {
		List<String> pathList = new ArrayList<String>();
		for (JarEntry je : classes) {
			pathList.add(je.getName().replace("WebContent/", ""));
		}
		return pathList;
	}
}
