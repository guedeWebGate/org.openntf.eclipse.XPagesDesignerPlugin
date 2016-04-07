package org.openntf.eclipse.xpdesigner.core.loaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.openntf.eclipse.xpdesigner.ui.projectwizard.Activator;

public class TargetBundleClassLoader extends URLClassLoader {

	private final List<ClassLoader> classLoader = new ArrayList<ClassLoader>();
	private final List<URI> bundleURIs = new ArrayList<URI>();

	public TargetBundleClassLoader(List<URI> locations, ClassLoader parent) throws MalformedURLException {
		super(buildURLS(locations), parent);
		this.bundleURIs.addAll(locations);
		readingAllManifest(locations);
	}

	private void readingAllManifest(List<URI> location) {
		for (URI uri : location) {
			readingManifest(uri);
		}

	}

	private static URL[] buildURLS(List<URI> locations) throws MalformedURLException {
		Set<URL> urls = new HashSet<URL>();
		for (URI uri : locations) {
			urls.add(uri.toURL());
		}
		return urls.toArray(new URL[urls.size()]);
	}

	private void readingManifest(URI bundleURI) {
		Manifest mf = null;
		JarFile jf = null;
		try {
			File file = new File(bundleURI);
			if (file.isDirectory()) {
				File mfFile = new File(file.getAbsolutePath() + File.separator + "META-INF" + File.separator + "MANIFEST.MF");
				if (mfFile.exists()) {
					mf = new Manifest(new FileInputStream(mfFile));
				}
			} else {
				jf = new JarFile(file);
				mf = jf.getManifest();

			}

			if (mf != null) {
				processMFClassPath(bundleURI, mf, file);
			}
		} catch (MalformedURLException e) {
			Activator.getDefault().logException(e);
		} catch (IOException e) {
			Activator.getDefault().logException(e);
		} finally {
			if (jf != null) {
				try {
					jf.close();
				} catch (Exception e) {
					Activator.getDefault().logException(e);
				}
			}
		}
	}

	private void processMFClassPath(URI bundleURI, Manifest mf, File file) throws MalformedURLException {
		String classPath = mf.getMainAttributes().getValue("Bundle-ClassPath");
		if (classPath != null && !"".equals(classPath)) {
			String[] cps = classPath.split(",");
			for (String cp : cps) {
				if (!".".equals(cp)) {
					SimpleJarClassLoader sacl = new SimpleJarClassLoader(file, cp, this);
					classLoader.add(sacl);
				}
			}
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Activator.getDefault().log("searching ..... " + name + " class!");
		try {
			return super.findClass(name);
		} catch (Exception ex) {
			for (ClassLoader cl : classLoader) {
				try {
					Class<?> found = cl.loadClass(name);
					if (found != null) {
						return found;
					}
				} catch (ClassNotFoundException e) {
					// No Catch needed
				}
			}
		}
		throw new ClassNotFoundException(name + " not found in the XPDE Target");
	}


	@Override
	public URL findResource(String name) {
		Activator.getDefault().log("searching for ..... " + name + " resource!");
		URL result = super.findResource(name);
		if (result != null) {
			Activator.getDefault().log("Found via supr (" + name + ") " + result.toString());
			return result;

		}
		for (ClassLoader cl : classLoader) {
			URL jarresult = cl.getResource(name);
			if (jarresult != null) {
				Activator.getDefault().log("Found (" + name + ") " + jarresult.toString());
				return jarresult;
			}
		}
		return null;
	}
}
