package org.openntf.eclipse.xpdesigner.core;

import org.eclipse.core.resources.IFile;
import org.openntf.eclipse.xpde.loader.LoaderActivator;
import org.openntf.eclipse.xpdesigner.core.compiler.XSPClass;
import org.openntf.eclipse.xpdesigner.core.compiler.XSPClassBuilder;
import org.osgi.framework.Bundle;

public enum XDECommandWrapper {
	INSTANCE;

	public XSPClass compileFile(IFile file) throws Exception {

		XSPClassBuilder classBuilder = new XSPClassBuilder();
		XSPClass xspClass = classBuilder.compileFile(file);
		return xspClass;
		/*
		 * ClassLoader currentClassLoader =
		 * Thread.currentThread().getContextClassLoader(); try {
		 * 
		 * Bundle ibmCoreBundle =
		 * LoaderActivator.getDefault().getIbmCoreBundle(); Class<?> clReg =
		 * ibmCoreBundle.loadClass("com.ibm.xsp.registry.FacesRegistry");
		 * CoreActivator.getDefault().log("REG -> "+clReg);
		 * Thread.currentThread().setContextClassLoader(clReg.getClassLoader());
		 * XSPClassBuilder classBuilder = new XSPClassBuilder(); XSPClass
		 * xspClass = classBuilder.compileFile(file); return xspClass; } catch
		 * (Exception ex) { CoreActivator.getDefault().logException(ex); throw
		 * ex; } finally {
		 * Thread.currentThread().setContextClassLoader(currentClassLoader); }
		 */
	}
}
