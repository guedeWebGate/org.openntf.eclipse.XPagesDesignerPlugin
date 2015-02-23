package org.openntf.eclipse.xpdesigner.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.part.MultiPageEditorPart;

public class XDEMultiPageXSPEditor extends MultiPageEditorPart {

	private XDESourceEditor4XSP m_XspSourceEditor;
	private XDEVisualEditor4XSP m_XspVisualEditor;

	@Override
	protected void createPages() {
		try {
			m_XspSourceEditor = new XDESourceEditor4XSP();
			int index = addPage(m_XspSourceEditor, getEditorInput());
			setPageText(index, "Source: " + m_XspSourceEditor.getTitle());
			setPartName(m_XspSourceEditor.getTitle());
			m_XspVisualEditor = new XDEVisualEditor4XSP();
			index = addPage(m_XspVisualEditor, getEditorInput());
			setPageText(index, "Design: " + m_XspVisualEditor.getTitle());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		m_XspSourceEditor.doSave(monitor);

	}

	@Override
	public void doSaveAs() {
		m_XspSourceEditor.doSaveAs();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return m_XspSourceEditor.isSaveAsAllowed();
	}

}
