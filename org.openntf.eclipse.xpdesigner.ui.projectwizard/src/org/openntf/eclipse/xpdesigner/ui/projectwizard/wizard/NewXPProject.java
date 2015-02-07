package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.XPagesProjectSupport;

public class NewXPProject extends Wizard implements INewWizard,
		IExecutableExtension {

	private WizardNewProjectCreationPage m_PageOne;
	private IConfigurationElement m_Config;

	public NewXPProject() {
		setWindowTitle(Messages.NewXPProject_WINDOW_TITLE);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performFinish() {
		String name = m_PageOne.getProjectName();
		URI location = null;
		if (!m_PageOne.useDefaults()) {
			location = m_PageOne.getLocationURI();
		} // else location == null

		XPagesProjectSupport.createProject(name, location);

		BasicNewProjectResourceWizard.updatePerspective(m_Config);
		return true;
	}

	@Override
	public void addPages() {
		m_PageOne = new WizardNewProjectCreationPage(
				Messages.NewXPProject_WIZARD_NAME);
		m_PageOne.setTitle(Messages.NewXPProject_WIZARD_TITLE);
		m_PageOne.setDescription(Messages.NewXPProject_WIZARD_DESCRIPTION);

		addPage(m_PageOne);
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		m_Config = config;

	}
}
