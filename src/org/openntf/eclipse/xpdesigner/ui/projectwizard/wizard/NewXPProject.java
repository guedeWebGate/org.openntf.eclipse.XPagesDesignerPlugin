package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class NewXPProject extends Wizard implements INewWizard {

	private WizardNewProjectCreationPage m_PageOne;
	public NewXPProject() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPages() {
		m_PageOne = new WizardNewProjectCreationPage("From Scratch Project Wizard");
	    m_PageOne.setTitle("From Scratch Project");
	    m_PageOne.setDescription("Create something from scratch.");
	 
	    addPage(m_PageOne);
	}
}
