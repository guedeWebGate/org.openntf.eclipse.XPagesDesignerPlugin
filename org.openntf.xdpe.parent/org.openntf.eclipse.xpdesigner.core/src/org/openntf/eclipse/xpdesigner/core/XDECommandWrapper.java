package org.openntf.eclipse.xpdesigner.core;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.openntf.eclipse.xpdesigner.core.compiler.XSPClass;
import org.openntf.eclipse.xpdesigner.core.compiler.XSPClassBuilder;

public enum XDECommandWrapper {
	INSTANCE;

	public XSPClass compileFile(IFile file) throws CoreException, IOException {
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(CoreActivator.class.getClassLoader());
			XSPClassBuilder classBuilder = new XSPClassBuilder();
			XSPClass xspClass = classBuilder.compileFile(file);
			return xspClass;
		} finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
	}
}
