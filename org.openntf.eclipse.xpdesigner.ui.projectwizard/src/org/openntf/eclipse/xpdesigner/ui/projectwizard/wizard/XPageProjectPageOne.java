package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.openntf.eclipse.xpdesigner.core.XPagesComponentProvider;
import org.openntf.eclipse.xpdesigner.core.xspcomponents.XSPLibrary;

public class XPageProjectPageOne extends WizardNewProjectCreationPage {

	private Table m_Table;

	public XPageProjectPageOne(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite comp = (Composite) getControl();
		// Composite container = new Composite(comp, SWT.BORDER);
		// container.setLayout(new GridLayout(1, true));
		Label lbl = new Label(comp, SWT.NONE);
		lbl.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		lbl.setText("Extension Libraries");
		m_Table = new Table(comp, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		List<XSPLibrary> allLibs = XPagesComponentProvider.INSTANCE.scanPlugins4XSPLibraries();
		for (XSPLibrary lib : allLibs) {
			TableItem item = new TableItem(m_Table, SWT.NONE);
			item.setText(lib.getLib().getLibraryId());
			item.setData(lib);
		}
		m_Table.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//m_Table.setSize(500, 100);
	}

	public List<XSPLibrary> getSelectedXSPLibraries() {
		List<XSPLibrary> libs = new LinkedList<XSPLibrary>();
		for (TableItem item : m_Table.getItems()) {
			if (item.getChecked()) {
				libs.add((XSPLibrary) item.getData());
			}
		}
		return libs;
	}
}
