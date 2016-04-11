package org.openntf.eclipse.xpdesigner.core;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.palette.PaletteRoot;
import org.openntf.eclipse.xpdesigner.core.compiler.XSPClass;
import org.openntf.eclipse.xpdesigner.core.compiler.XSPClassBuilder;
import org.openntf.eclipse.xpdesigner.core.loaders.TargetBundleClassLoader;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDELibrary;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.Activator;

public class XDPEComponentProvider implements XDPEComponentLoader {

	private static final XDPEComponentProvider provider = new XDPEComponentProvider();
	private Object loader;

	public static synchronized XDPEComponentProvider getInstance() {
		return provider;
	}

	@Override
	public List<XDELibrary> scanPlugins4XSPLibraries() {
		checkLoader();
		return ((XDPEComponentLoader) loader).scanPlugins4XSPLibraries();
	}

	@Override
	public PaletteRoot buildPaletteRoot() throws MalformedURLException {
		checkLoader();
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			ClassLoader xspClassLoader = Activator.getDefault().getXSPClassLoader();
			Thread.currentThread().setContextClassLoader(xspClassLoader);
			return ((XDPEComponentLoader) loader).buildPaletteRoot();
		} finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
	}

	private synchronized void checkLoader() {

		if (loader == null) {
			Activator.getDefault().log("Loading XDPE ComponentLoader");
			ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
			try {
				ClassLoader xspClassLoader = Activator.getDefault().getXSPClassLoader();
				Thread.currentThread().setContextClassLoader(xspClassLoader);
				try {
					Class libInterface = xspClassLoader.loadClass("com.ibm.xsp.library.XspLibrary");
					Class libReb = xspClassLoader.loadClass("com.ibm.xsp.registry.FacesRegistry");
					Activator.getDefault().log("XspLibrary: " + libInterface);
					Activator.getDefault().log("FacesRegitry: " + libReb);
					Class libSReg = xspClassLoader.loadClass("com.ibm.xsp.registry.FacesSharableRegistry");
					Activator.getDefault().log("FacesSharableRegistry: " + libSReg);
				} catch (Exception ex) {
					Activator.getDefault().logException(ex);
				}
				Class<?> loaderClass = xspClassLoader.loadClass("org.openntf.eclipse.xpdesigner.core.XDPEComponentLoaderImpl");
				Activator.getDefault().log("Class loader of: " + loaderClass.getClassLoader());
				loader = loaderClass.newInstance();
			} catch (Exception ex) {
				ex.printStackTrace();
				Activator.getDefault().logException(ex);
			} finally {
				Thread.currentThread().setContextClassLoader(currentClassLoader);
			}
		}
	}

	public XSPClass compileFile(final IFile file) throws CoreException, IOException {
		checkLoader();
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			ClassLoader xspClassLoader = Activator.getDefault().getXSPClassLoader();
			Thread.currentThread().setContextClassLoader(xspClassLoader);
			Class<?> builderClass = xspClassLoader.loadClass("org.openntf.eclipse.xpdesigner.core.compiler.XSPClassBuilder");
			Class<?> loaderClass = xspClassLoader.loadClass("org.openntf.eclipse.xpdesigner.core.XDPEComponentLoaderImpl");
			Constructor<?> conBC = builderClass.getConstructor(loader.getClass());
			Object builder = conBC.newInstance(loader);
			Method compileFileMethod = builderClass.getMethod("compileFile", Object.class);
			return (XSPClass) compileFileMethod.invoke(builder, file);
		} catch (ClassNotFoundException e) {
			Activator.getDefault().logException(e);
		} catch (InstantiationException e) {
			Activator.getDefault().logException(e);
		} catch (IllegalAccessException e) {
			Activator.getDefault().logException(e);
		} catch (IllegalArgumentException e) {
			Activator.getDefault().logException(e);
		} catch (InvocationTargetException e) {
			Activator.getDefault().logException(e);
		} catch (NoSuchMethodException e) {
			Activator.getDefault().logException(e);
		} catch (SecurityException e) {
			Activator.getDefault().logException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
		return null;
	}

}
