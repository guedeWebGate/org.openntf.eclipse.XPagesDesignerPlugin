package org.openntf.eclipse.xpdesigner.core.definitions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public interface IDefinition {

	Properties getProperties();

	void loadProperties(InputStream is) throws IOException;

}