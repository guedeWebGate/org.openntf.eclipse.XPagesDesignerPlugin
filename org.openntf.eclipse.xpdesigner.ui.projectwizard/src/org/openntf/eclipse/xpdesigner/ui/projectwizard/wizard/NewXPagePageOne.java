package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewXPagePageOne extends WizardPage {

	private static final String REGEX_PATTERN = "^[A-Za-z0-9_]+$";

	private Text m_Filename;
	private Text m_Comment;
	private Combo m_Projects;
	private IStructuredSelection m_Selection;
	private boolean m_Valid = false;

	protected NewXPagePageOne(String pageName, IStructuredSelection selection) {
		super(pageName);
		m_Selection = selection;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout gdl = new GridLayout(2, false);
		comp.setLayout(gdl);

		Label lab = new Label(comp, SWT.NONE);
		lab.setText("Name:");
		GridData gd = new GridData();
		gd.widthHint = 100;
		lab.setLayoutData(gd);
		m_Filename = new Text(comp, SWT.BORDER);
		m_Filename.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (!m_Filename.getText().matches(REGEX_PATTERN)) {
					setMessage("A name must start with a letter or underscore (_) and can only contain numbers, letters and underscores (_).", DialogPage.ERROR);
					m_Valid = false;
					return;
				}
				if ("".equals(m_Filename.getText().trim())) {
					m_Valid = false;
				}

				setMessage("Create a new XPage.", DialogPage.INFORMATION);
			}
		});

		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_Filename.setLayoutData(gd);
		Label lab2 = new Label(comp, SWT.NONE);
		lab2.setText("Comment:");

		m_Comment = new Text(comp, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_Comment.setLayoutData(gd);
		setControl(comp);
		
		Label lab3 = new Label(comp, SWT.NONE);
		lab.setText("XPage Project:");
		m_Projects = new Combo(comp, SWT.READ_ONLY);
		

	}

	public Text getFilename() {
		return m_Filename;
	}

	public void setFilename(Text filename) {
		m_Filename = filename;
	}

	public Text getComment() {
		return m_Comment;
	}

	public void setComment(Text comment) {
		m_Comment = comment;
	}

	@Override
	public boolean isPageComplete() {
		return m_Valid;
	}
}
