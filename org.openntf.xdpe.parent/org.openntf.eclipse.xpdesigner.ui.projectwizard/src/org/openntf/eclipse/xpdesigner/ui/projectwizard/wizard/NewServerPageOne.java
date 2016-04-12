package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.ServerDefinitionSupport;

public class NewServerPageOne extends WizardPage {
	private static final String REGEX_PATTERN_SERVERNAME = "^[A-Za-z0-9_]+$";
	private static final String REGEX_PATTERN_PORT = "^(0|[1-9][0-9]*)$";

	private Text m_ServerName;
	private Text m_ServerFQDN;
	private Text m_Port;

	protected NewServerPageOne(String pageName) {
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
		lab.setText("Server name:");
		lab.setLayoutData(gd);
		m_ServerName = new Text(comp, SWT.BORDER);
		m_ServerName.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}

		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_ServerName.setLayoutData(gd);

		Label lab2 = new Label(comp, SWT.NONE);
		lab2.setText("Server FQDN/IP Address:");

		m_ServerFQDN = new Text(comp, SWT.BORDER);
		m_ServerFQDN.setText("localhost");
		m_ServerFQDN.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();

			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_ServerFQDN.setLayoutData(gd);

		Label lab3 = new Label(comp, SWT.NONE);
		lab3.setText("PORT:");

		m_Port = new Text(comp, SWT.BORDER);
		m_Port.setText("80");
		m_Port.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}

		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_Port.setLayoutData(gd);
		setControl(comp);
		setPageComplete(false);
	}

	protected void validate() {
		if (!m_ServerName.getText().matches(REGEX_PATTERN_SERVERNAME)) {
			setMessage("A name must start with a letter or underscore (_) and can only contain numbers, letters and underscores (_).", DialogPage.ERROR);
			setPageComplete(false);
			return;
		}
		if (!StringUtils.isEmpty(m_ServerName.getText()) && ServerDefinitionSupport.INSTANCE.checkHasServerName(m_ServerName.getText())) {
			setMessage("A server with this name already exists.");
			setPageComplete(false);
			return;
		}
		if (!m_Port.getText().matches(REGEX_PATTERN_PORT)) {
			setMessage("A port must contain numbers", DialogPage.ERROR);
			setPageComplete(false);
			return;
		}
		if (m_ServerFQDN.getText().trim().length() < 3) {
			setMessage("A FQDN must contain at least 3 characters", DialogPage.ERROR);
			setPageComplete(false);
			return;
		}
		setMessage("Create a new XWork / Domion Server definition", DialogPage.INFORMATION);
		setPageComplete(true);

	}

	public String getServerName() {
		return m_ServerName.getText();
	}

	public String getServerFQDN() {
		return m_ServerFQDN.getText();
	}

	public String getPort() {
		return m_Port.getText();
	}

}
