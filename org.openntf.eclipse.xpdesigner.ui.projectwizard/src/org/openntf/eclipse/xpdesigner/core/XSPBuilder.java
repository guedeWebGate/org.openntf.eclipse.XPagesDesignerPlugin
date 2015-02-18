package org.openntf.eclipse.xpdesigner.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.eclipse.xpdesigner.ui.natures.ProjectNature;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.XSPFileBuilder;

import com.ibm.xsp.page.compiled.PageToClassNameUtil;
import com.ibm.xsp.page.parse.ComponentElement;
import com.ibm.xsp.page.parse.FacesDeserializer;
import com.ibm.xsp.page.parse.FacesReader;
import com.ibm.xsp.page.translator.LogicalPage;
import com.ibm.xsp.page.translator.PhysicalPage;
import com.ibm.xsp.page.translator.Translator;
import com.ibm.xsp.registry.FacesSharableRegistry;

public class XSPBuilder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "org.openntf.eclipse.xpdesigner.core.XSPBuilder";

	class DeltaVisitor implements IResourceDeltaVisitor {
		private IProgressMonitor monitor;
		
		public DeltaVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public boolean visit(IResourceDelta delta) {
			IResource resource = delta.getResource();
			if (resource instanceof IProject) {
				IProject project = (IProject) resource;
				try {
					return project.hasNature(ProjectNature.NATURE_ID);
				} catch (CoreException e) {
					e.printStackTrace();
					return false;
				}
			}
			if (resource instanceof IFolder) {
				IFolder folder = (IFolder) resource;
				if (checkFolderName(folder)) {
					return true;
				}
			}
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				if (file.getParent() instanceof IFolder && "xsp".equals(file.getFileExtension())) {
					System.out.println("BINGO....");
					try {
						compileFile(file);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					return true;
				}
			}
			return false;
		}

		private boolean checkFolderName(IFolder folder) {
			return "XPages".equals(folder.getName()) || "CustomControls".equals(folder.getName());
		}
	}

	public XSPBuilder() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		System.out.println("XSP Build is called");
		System.out.println("Kind: " + kind);
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new DeltaVisitor(monitor));

	}

	private void fullBuild(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	private void compileFile(IFile file) throws CoreException, IOException {
        FacesSharableRegistry reg = XPagesComponentProvider.INSTANCE.getRegistry();
		Map<String, Object> options = new HashMap<String, Object>();
		// allowNamespacedMarkupTags defaults to true in FacesDeserializer
		// but defaults to false in the design-time code.
		options.put(FacesDeserializer.OPTION_ALLOW_NAMESPACED_MARKUP_TAGS, true);
		FacesDeserializer deserial = new FacesDeserializer(reg, options);
		ComponentElement root;
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
			return;
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
		String result = compiler.translate(logical);
		XSPFileBuilder.INSTANCE.createJavaFileForXSP(getProject(), className,result);
	}

}
