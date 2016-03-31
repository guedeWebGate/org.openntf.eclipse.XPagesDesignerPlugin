package org.openntf.eclipse.xpdesigner.core.commands;

import java.io.ByteArrayOutputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openntf.eclipse.xpdesigner.core.PackageBuilder;
import org.openntf.eclipse.xpdesigner.core.PackagePublisher;
import org.openntf.eclipse.xpdesigner.core.publish.Instructions;

public class JarPackageBuilder extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(arg0);
		if (window != null) {
			IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IAdaptable) {
				IProject project = (IProject) ((IAdaptable) firstElement).getAdapter(IProject.class);
				IPath path = project.getFullPath();
				System.out.println(path);
				try {
					ByteArrayOutputStream bos = PackageBuilder.INSTANCE.buildPackage(project);
					//TODO: Customize this
					Instructions instr = new Instructions("test/todo.nsf");
					PackagePublisher publisher = new PackagePublisher("http://cgu_srv9/publisher.cmd", instr);
					publisher.publish(bos, "John Builder/WGCDEV/CH", "buildIt");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return null;
	}

}
