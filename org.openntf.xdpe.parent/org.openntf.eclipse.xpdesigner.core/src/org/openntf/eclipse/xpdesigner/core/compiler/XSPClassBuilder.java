package org.openntf.eclipse.xpdesigner.core.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.openntf.eclipse.xpdesigner.core.XDPEComponentLoaderImpl;
import org.openntf.eclipse.xpdesigner.core.XPagesComponentProvider;

import com.ibm.xsp.page.compiled.PageToClassNameUtil;
import com.ibm.xsp.page.parse.ComponentElement;
import com.ibm.xsp.page.parse.FacesDeserializer;
import com.ibm.xsp.page.parse.FacesReader;
import com.ibm.xsp.page.translator.LogicalPage;
import com.ibm.xsp.page.translator.PhysicalPage;
import com.ibm.xsp.page.translator.Translator;
import com.ibm.xsp.registry.FacesSharableRegistry;

public class XSPClassBuilder {

	private final XDPEComponentLoaderImpl loader;

	public XSPClassBuilder() {
		loader =null;
	}
	public XSPClassBuilder(XDPEComponentLoaderImpl loader) {
		this.loader = loader;
	}

	public XSPClass compileFile(Object ofile) throws CoreException, IOException {
		FacesSharableRegistry reg = loader != null? loader.getRegistry(): XPagesComponentProvider.INSTANCE.getRegistry();
		Map<String, Object> options = new HashMap<String, Object>();
		// allowNamespacedMarkupTags defaults to true in FacesDeserializer
		// but defaults to false in the design-time code.
		options.put(FacesDeserializer.OPTION_ALLOW_NAMESPACED_MARKUP_TAGS, true);
		FacesDeserializer deserial = new FacesDeserializer(reg, options);
		ComponentElement root;
		IFile file = (IFile)ofile;
		try {
			InputStream in = file.getContents();
			try {
				FacesReader reader = new FacesReader(in);

				root = deserial.readRoot(reader);

			} finally {
				in.close();
			}
			if (root == null) {
				throw new RuntimeException("Unable to deserialize the file : " + file.getName());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		Map<String, Object> options2 = new HashMap<String, Object>();
		options2.put(Translator.OPTION_APPLICATION_VERSION, null);
		options2.put(Translator.OPTION_ERROR_HANDLER, null);
		Translator compiler = new Translator(reg, options2);
		String className = PageToClassNameUtil.getClassNameForPage(file.getName());
		LogicalPage logical = new LogicalPage(className, file.getName(), false);
		PhysicalPage physical = new PhysicalPage("", root, "", 0);
		logical.addMainPage(physical);

		// generate the .java class
		String code = compiler.translate(logical);
		XSPClass xspClass = new XSPClass(className, code);
		return xspClass;

	}

}
