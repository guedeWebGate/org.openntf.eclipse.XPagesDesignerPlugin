package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.XSPFileBuilder;

public abstract class AbstractNewXSPElement extends Wizard implements INewWizard {

	private NewXPagePageOne m_PageOne;
	private IStructuredSelection m_Selection;
	private IWorkbench m_CurrentWorkbench;

	public AbstractNewXSPElement() {
		super();
		setWindowTitle("New " + getXSPTitle());
	}

	public abstract String getTargetFolder();

	public abstract String getXSPTitle();

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		m_Selection = selection;
		m_CurrentWorkbench = workbench;
	}

	@Override
	public boolean performFinish() {
		String xpageName = m_PageOne.getFilename().getText();
		try {
			IFile file = XSPFileBuilder.INSTANCE.createFile(m_PageOne.getSelectedProject(), getTargetFolder(), xpageName);
			openFile(file);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return true;
	}

	@Override
	public void addPages() {
		m_PageOne = new NewXPagePageOne("New " + getXSPTitle(), m_Selection, getXSPTitle());
		m_PageOne.setTitle("New " + getXSPTitle());
		m_PageOne.setDescription("Create a new " + getXSPTitle() + ".");
		addPage(m_PageOne);
	}

	private void openFile(final IFile file) {
		m_CurrentWorkbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				final IWorkbenchWindow ww = m_CurrentWorkbench.getActiveWorkbenchWindow();
				final IWorkbenchPage page = ww.getActivePage();
				if (page == null)
					return;
				IWorkbenchPart focusPart = page.getActivePart();
				if (focusPart instanceof ISetSelectionTarget) {
					ISelection selection = new StructuredSelection(file);
					((ISetSelectionTarget) focusPart).selectReveal(selection);
				}
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
	}

}