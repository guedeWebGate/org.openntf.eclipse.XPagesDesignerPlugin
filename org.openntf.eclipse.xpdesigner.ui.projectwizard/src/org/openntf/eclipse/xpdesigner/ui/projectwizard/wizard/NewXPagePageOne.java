package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewXPagePageOne extends WizardPage {

	private Text m_Filename;
	private Text m_Comment;

	protected NewXPagePageOne(String pageName) {
		super(pageName);
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

		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_Filename.setLayoutData(gd);
		Label lab2 = new Label(comp, SWT.NONE);
		lab2.setText("Comment:");

		m_Comment = new Text(comp, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_Comment.setLayoutData(gd);
		setControl(comp);

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

}
