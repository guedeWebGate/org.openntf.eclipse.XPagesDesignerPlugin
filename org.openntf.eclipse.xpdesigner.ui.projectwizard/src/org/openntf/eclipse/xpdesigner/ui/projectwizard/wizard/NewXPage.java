package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.openntf.eclipse.xpdesigner.core.XPagesComponentProvider;
import org.openntf.eclipse.xpdesigner.core.xspcomponents.XSPLibrary;

public class NewXPage extends Wizard implements INewWizard {

	private NewXPagePageOne m_PageOne;
	private IStructuredSelection m_Selection;

	public NewXPage() {
		setWindowTitle("New XPage");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		m_Selection = selection;

	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPages() {
		try {
			List<XSPLibrary> libs = XPagesComponentProvider.INSTANCE.scanPlugins4XSPLibraries();
			for (XSPLibrary lib : libs) {
				System.out.println(lib.getPluginID() + " --> " + lib.getClassName());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		m_PageOne = new NewXPagePageOne("New XPage");
		m_PageOne.setTitle("New XPage");
		m_PageOne.setDescription("Please insert the filename");
		
		addPage(m_PageOne);
	}


}
