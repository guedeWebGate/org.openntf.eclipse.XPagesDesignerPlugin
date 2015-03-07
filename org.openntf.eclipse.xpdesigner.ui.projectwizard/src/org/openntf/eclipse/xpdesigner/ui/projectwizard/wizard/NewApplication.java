package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.ApplicationDefinitionSupport;

public class NewApplication extends Wizard implements INewWizard {

	private NewApplicationPageOne m_PageOne;

	public NewApplication() {
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performFinish() {
		ApplicationDefinitionSupport.INSTANCE.initApplicationDefinition(m_PageOne.getApplicationName(), m_PageOne.getAutoUpdate(), m_PageOne.getPathToNSF(), m_PageOne.getTargetServer(), m_PageOne.getXPagesProject());
		return true;
	}

	@Override
	public void addPages() {
		m_PageOne = new NewApplicationPageOne("Application Deployment");
		addPage(m_PageOne);
		m_PageOne.setMessage("Create a new Application Mapping Definition", DialogPage.INFORMATION);
	}
}
