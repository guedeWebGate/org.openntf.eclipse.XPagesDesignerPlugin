package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.XSPBuilder;

public class NewXPage extends Wizard implements INewWizard {

	private NewXPagePageOne m_PageOne;
	private IStructuredSelection m_Selection;

	public NewXPage() {
		setWindowTitle("New XPage");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		m_Selection = selection;
		System.out.println("Instance of IResource: "+ (selection.getFirstElement() instanceof IResource));
		System.out.println("Instance of IAdaptable: "+ (selection.getFirstElement() instanceof IAdaptable));
		

	}

	@Override
	public boolean performFinish() {
		String xpageName = m_PageOne.getFilename() +".xsp";
		XSPBuilder.INSTANCE.createFile("XPages", xpageName);
		return false;
	}

	@Override
	public void addPages() {
		m_PageOne = new NewXPagePageOne("New XPage", m_Selection);
		m_PageOne.setTitle("New XPage");
		m_PageOne.setDescription("Create a new XPage.");		
		addPage(m_PageOne);
	}


}
