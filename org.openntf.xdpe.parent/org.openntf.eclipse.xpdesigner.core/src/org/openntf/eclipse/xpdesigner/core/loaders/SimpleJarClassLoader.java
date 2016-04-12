package org.openntf.eclipse.xpdesigner.core.loaders;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import org.openntf.eclipse.xpdesigner.core.CoreActivator;

public class SimpleJarClassLoader extends ClassLoader {

	private final File bundleFileOrPath;
	private final String pathInJar;
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

	public SimpleJarClassLoader(File bundleFileOrPath, String pathInJar, ClassLoader parent) {
		super(parent); // calls the parent class loader's constructor
		this.bundleFileOrPath = bundleFileOrPath;
		this.pathInJar = pathInJar;
	}

	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return findClass(className);
	}

	@Override
	public Class<?> findClass(String className) {
		Class<?> result = null;

		if (classes.containsKey(className)) {
			result = classes.get(className); // checks in cached classes
			return result;
		}
		IContentReader cntReader = buildContentReader();
		try {
			
			JarInputStream jarFileStream = cntReader.buildInputStreamReader();
			JarEntry entry = jarFileStream.getNextJarEntry();
			while (entry != null) {
				if (entry.getName().equals(className + ".class")) {
					result = buildClassFromJarInputStream(className, jarFileStream);
					entry = null;
				} else {
					jarFileStream.getNextJarEntry();
				}
			}
			jarFileStream.close();
			if (result != null) {
				classes.put(className, result);

				return result;
			}

		} catch (Exception e) {
			CoreActivator.getDefault().logException(e);
			return null;
		} finally {
			try {
				cntReader.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	private IContentReader buildContentReader() {
		if (bundleFileOrPath.isDirectory()) {
			return new IContentReader() {
				@Override
				public void close() {
					//Not used
				}

				@Override
				public JarInputStream buildInputStreamReader() throws IOException {
					return new JarInputStream(new FileInputStream(bundleFileOrPath.getAbsolutePath() + File.pathSeparator + pathInJar));

				}
			};
		} else {
			return new IContentReader() {
				JarFile jarFile = null;

				@Override
				public void close() throws IOException {
					if (jarFile != null) {
						jarFile.close();
					}
				}

				@Override
				public JarInputStream buildInputStreamReader() throws IOException {
					jarFile = new JarFile(bundleFileOrPath);
					ZipEntry entry = jarFile.getEntry(pathInJar);
					return new JarInputStream(jarFile.getInputStream(entry));
				}
			};

		}

	}

	private Class<?> buildClassFromJarInputStream(String className, JarInputStream jarFileStream) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		int nextValue = jarFileStream.read();
		while (-1 != nextValue) {
			byteStream.write(nextValue);
			nextValue = jarFileStream.read();
		}

		byte[] classByte = byteStream.toByteArray();
		return defineClass(className, classByte, 0, classByte.length, null);
	}

	@Override
	public URL findResource(String name) {
		try {
			String path;
			if (bundleFileOrPath.isDirectory()) {
				path = bundleFileOrPath.toURI().toURL().toString() + File.separator + pathInJar + "!/" + name;
			} else {
				path = bundleFileOrPath.toURI().toURL().toString() + "!" + File.separator + pathInJar + "!/" + name;
			}
			return new URL("jar:" + path);
		} catch (Exception e) {
			return null;
		}

	}
}
