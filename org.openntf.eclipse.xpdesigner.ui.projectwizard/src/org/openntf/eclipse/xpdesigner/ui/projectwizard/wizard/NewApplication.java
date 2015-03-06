package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewApplication extends Wizard implements INewWizard {

	public NewApplication() {
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
		NewApplicationPageOne m_PageOne = new NewApplicationPageOne("Application Deployment");
		addPage(m_PageOne);
	}
}
