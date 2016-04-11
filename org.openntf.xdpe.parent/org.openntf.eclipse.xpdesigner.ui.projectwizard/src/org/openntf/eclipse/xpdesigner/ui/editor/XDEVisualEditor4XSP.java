package org.openntf.eclipse.xpdesigner.ui.editor;

import java.net.MalformedURLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.openntf.eclipse.xpdesigner.core.XDPEComponentProvider;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.Activator;

public class XDEVisualEditor4XSP extends GraphicalEditorWithFlyoutPalette {

	private XDEMultiPageXSPEditor parentEditor;

	public XDEVisualEditor4XSP() {
		DefaultEditDomain def = new DefaultEditDomain(this);
		setEditDomain(new DefaultEditDomain(this));
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		return buildRoot();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	private PaletteRoot buildRoot() {
		try {
			return XDPEComponentProvider.getInstance().buildPaletteRoot();
		} catch (MalformedURLException e) {
			Activator.getDefault().logException(e);
		}
		return null;
	}
}
