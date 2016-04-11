package org.openntf.eclipse.xpdesigner.core.loaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.openntf.eclipse.xpdesigner.ui.projectwizard.Activator;

public class TargetBundleClassLoader extends URLClassLoader {

	private final List<URI> bundleURIs = new ArrayList<URI>();

	public TargetBundleClassLoader(List<URI> locations, ClassLoader parent, String symbolicName) throws MalformedURLException {
		super(buildURLS(locations, symbolicName), parent);
		this.bundleURIs.addAll(locations);
	}

	private static URL[] buildURLS(List<URI> locations, String symbolicName) throws MalformedURLException {
		Set<URL> urls = new HashSet<URL>();
		for (URI uri : locations) {
			urls.add(uri.toURL());
			urls.addAll(readingManifest(uri, symbolicName));
		}
		return urls.toArray(new URL[urls.size()]);
	}

	private static Set<URL> readingManifest(URI bundleURI, String symbolicName) {
		Manifest mf = null;
		JarFile jf = null;
		Set<URL> urls = new HashSet<URL>();
		File fileTempDir = new File(Activator.getDefault().getNewTempSubDir(symbolicName));
		try {
			File file = new File(bundleURI);
			if (file.isDirectory()) {
				File mfFile = new File(file.getAbsolutePath() + File.separator + "META-INF" + File.separator + "MANIFEST.MF");
				if (mfFile.exists()) {
					mf = new Manifest(new FileInputStream(mfFile));
					processMFClassPathDirectory(urls, mf, file);
				}
			} else {
				jf = new JarFile(file);
				mf = jf.getManifest();
				if (mf != null) {
					prcoessMVClassPathJar(urls, jf, mf, fileTempDir);
				}
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
		return urls;
	}

	private static void prcoessMVClassPathJar(Set<URL> urls, JarFile jf, Manifest mf, File fileTempDir) {
		String classPath = mf.getMainAttributes().getValue("Bundle-ClassPath");
		if (classPath != null && !"".equals(classPath)) {
			String[] cps = classPath.split(",");
			for (String cp : cps) {
				if (!".".equals(cp)) {
					JarEntry jentry = jf.getJarEntry(cp);
					if (jentry != null) {
						File target = new File(fileTempDir.getAbsolutePath() + File.separator + jentry.getName());
						target.getParentFile().mkdirs();
						try {
							InputStream is = jf.getInputStream(jentry);
							FileOutputStream fos = new FileOutputStream(target);
							while (is.available() > 0) {
								fos.write(is.read());
							}
							fos.close();
							is.close();
							urls.add(target.toURI().toURL());
						} catch (Exception ex) {
							Activator.getDefault().logException(ex);
						}
					} else {
						Activator.getDefault().log("INFO: " + cp + " not found!");
					}
				}
			}
		}
	}

	private static void processMFClassPathDirectory(Set<URL> urls, Manifest mf, File file) {
		String classPath = mf.getMainAttributes().getValue("Bundle-ClassPath");
		if (classPath != null && !"".equals(classPath)) {
			String[] cps = classPath.split(",");
			for (String cp : cps) {
				if (!".".equals(cp)) {
					String path = file.getAbsolutePath() + File.separator + cp;
					File jFile = new File(path);
					try {
						urls.add(jFile.toURI().toURL());
					} catch (MalformedURLException e) {
						Activator.getDefault().log("Could not load " + path);
					}
				}
			}
		}

	}
	
}
