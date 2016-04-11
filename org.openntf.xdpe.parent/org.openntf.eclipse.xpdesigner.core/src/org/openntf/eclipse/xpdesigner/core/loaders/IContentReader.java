package org.openntf.eclipse.xpdesigner.core.loaders;

import java.io.IOException;
import java.util.jar.JarInputStream;

public interface IContentReader {

	public JarInputStream buildInputStreamReader() throws IOException;
	public void close() throws IOException;
}
