package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.ServerDefinitionSupport;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.XPagesProjectSupport;

import com.ibm.commons.util.StringUtil;

public class NewApplicationPageOne extends WizardPage {
	private static final String REGEX_PATTERN_APPNAME = "^[A-Za-z0-9_]+$";

	private Text m_ApplicationName;
	private Combo m_XPagesProject;
	private Combo m_TargetServer;
	private Button m_AutoUpdate;
	private Map<String, IProject> m_ProjectList;

	protected NewApplicationPageOne(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout gdl = new GridLayout(2, false);
		comp.setLayout(gdl);

		GridData gd = new GridData();
		gd.widthHint = 100;

		Label lab = new Label(comp, SWT.NONE);
		lab.setText("Application Alias:");
		lab.setLayoutData(gd);
		m_ApplicationName = new Text(comp, SWT.BORDER);
		m_ApplicationName.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}

		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_ApplicationName.setLayoutData(gd);

		Label lab2 = new Label(comp, SWT.NONE);
		m_ProjectList = XPagesProjectSupport.buildXPagesProjectList();

		lab2.setText("XPages Project:");

		m_XPagesProject = new Combo(comp, SWT.READ_ONLY);
		m_XPagesProject.setItems(XPagesProjectSupport.getProjectsAsList(m_ProjectList).toArray(new String[0]));

		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_XPagesProject.setLayoutData(gd);
		m_XPagesProject.select(0);

		Label lab3 = new Label(comp, SWT.NONE);
		lab3.setText("Target Server:");

		m_TargetServer = new Combo(comp, SWT.READ_ONLY);
		m_TargetServer.setItems(ServerDefinitionSupport.INSTANCE.getServerDefinitionNames().toArray(new String[0]));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_TargetServer.setLayoutData(gd);
		
		Label lab4 = new Label(comp, SWT.NONE);
		lab3.setText("");

		m_AutoUpdate = new Button(comp, SWT.CHECK);
		m_AutoUpdate.setText("Update server application automatically");

		setControl(comp);
		setPageComplete(false);
	}

	protected void validate() {
	}

}
