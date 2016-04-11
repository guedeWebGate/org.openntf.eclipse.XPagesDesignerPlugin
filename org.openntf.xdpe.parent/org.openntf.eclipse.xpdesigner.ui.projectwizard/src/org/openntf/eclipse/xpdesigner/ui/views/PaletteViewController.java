package org.openntf.eclipse.xpdesigner.ui.views;

import java.net.MalformedURLException;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.views.palette.PaletteView;
import org.eclipse.swt.widgets.Composite;
import org.openntf.eclipse.xpdesigner.core.XPagesComponentProvider;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.Activator;

public class PaletteViewController extends PaletteView {

	public PaletteViewController() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		EditDomain editDomain=null;
		try {
			editDomain = buildEditDomain();
		} catch (MalformedURLException e) {
			Activator.getDefault().logException(e);
		}
		PaletteViewerProvider provider = new PaletteViewerProvider(editDomain);
		provider.createPaletteViewer(parent);
	}

	private EditDomain buildEditDomain() throws MalformedURLException {
		EditDomain domain = new EditDomain();
		domain.setPaletteRoot(XPagesComponentProvider.INSTANCE.buildPaletteRoot());
		return domain;
	}

}
