package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.openntf.eclipse.xpdesigner.core.definitions.ApplicationDefinitionSupport;

public class NewApplication extends Wizard implements INewWizard {

	private NewApplicationPageOne m_PageOne;

	public NewApplication() {
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public boolean performFinish() {
		String appName =m_PageOne.getApplicationName().getText().trim();
		String autoUpdate = m_PageOne.getAutoUpdate().getSelection() ? "TRUE":"FALSE"; 
		String pathToNsf =		m_PageOne.getPathToNSF().getText().trim();
		String targetServer = m_PageOne.getTargetServer().getText().trim(); 
		String xpagesProject = m_PageOne.getXPagesProject().getText().trim();
		ApplicationDefinitionSupport.INSTANCE.initApplicationDefinition(appName, autoUpdate, pathToNsf, targetServer, xpagesProject);
		return true;
	}

	@Override
	public void addPages() {
		m_PageOne = new NewApplicationPageOne("Application Deployment");
		addPage(m_PageOne);
		m_PageOne.setMessage("Create a new Application Mapping Definition", DialogPage.INFORMATION);
	}
}
