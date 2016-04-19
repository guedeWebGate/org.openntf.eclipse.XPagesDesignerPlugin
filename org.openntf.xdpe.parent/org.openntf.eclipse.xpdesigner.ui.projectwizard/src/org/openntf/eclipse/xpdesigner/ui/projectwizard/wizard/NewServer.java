package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.ServerDefinitionSupport;

public class NewServer extends Wizard implements INewWizard {

	private NewServerPageOne m_PageOne;
	public NewServer() {
		super();
		setWindowTitle("New XPages/Domino Server Definition");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		m_PageOne = new NewServerPageOne("XWork/Domino Server");
		m_PageOne.setDescription("Create a new XWork / Domion Server definition");
		addPage(m_PageOne);
	}
	@Override
	public boolean performFinish() {
		String serverName = m_PageOne.getServerName();
		String serverFQDN = m_PageOne.getServerFQDN();
		String port = m_PageOne.getPort();
		
		return ServerDefinitionSupport.INSTANCE.initServerDefinition(serverName, serverFQDN, port);
	}

}
