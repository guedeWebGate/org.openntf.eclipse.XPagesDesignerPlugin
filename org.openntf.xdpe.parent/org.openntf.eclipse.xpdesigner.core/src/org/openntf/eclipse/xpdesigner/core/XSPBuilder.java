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
import org.openntf.eclipse.xpdesigner.core.compiler.XSPClass;
import org.openntf.eclipse.xpdesigner.core.compiler.XSPClassBuilder;
import org.openntf.eclipse.xpdesigner.ui.natures.ProjectNature;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.Activator;
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
		Activator.getDefault().log("XSP Build is called");
		Activator.getDefault().log("Kind: " + kind);
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IProject project = getProject();
			IResourceDelta delta = getDelta(project);
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

		XSPClassBuilder classBuilder = new XSPClassBuilder();
		XSPClass xspClass = classBuilder.compileFile(file);
		if (xspClass == null) {
			Activator.getDefault().log("No xspClass build for " + file);
		}
		IFile javaFile = XSPFileBuilder.INSTANCE.createJavaFileForXSP(getProject(), xspClass.getName(), xspClass.getCode());
		/*
		XSPClass xspClass = XDPEComponentProvider.getInstance().compileFile(file);
		if (xspClass == null) {
			Activator.getDefault().log("No xspClass build for " + file);
		}
		IFile javaFile = XSPFileBuilder.INSTANCE.createJavaFileForXSP(getProject(), xspClass.getName(), xspClass.getCode());
		*/
	}

}
