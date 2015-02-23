package org.openntf.eclipse.xpdesigner.ui.editor;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteTemplateEntry;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openntf.eclipse.xpdesigner.core.XPagesComponentProvider;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.ExtensionCreatorFactory;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDEComponentElement;
import org.openntf.eclipse.xpdesigner.ui.views.PaletteViewController;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.registry.FacesComponentDefinition;

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
		PaletteRoot root = new PaletteRoot();
		Map<String, List<FacesComponentDefinition>> componentMap = XPagesComponentProvider.INSTANCE.getComponentMap();
		for (String key : componentMap.keySet()) {
			PaletteDrawer drawer = new PaletteDrawer(key);
			drawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
			drawer.setDrawerType(PaletteTemplateEntry.PALETTE_TYPE_TEMPLATE);
			root.add(drawer);
			List<FacesComponentDefinition> components = componentMap.get(key);
			for (FacesComponentDefinition component : components) {
				XDEComponentElement element = (XDEComponentElement) ExtensionCreatorFactory.COMPONENT.getExtensionFromNode(component);
				ImageDescriptor descrLarge = null;
				ImageDescriptor descrSmall = null;
				try {
					if (element.getIcon() != null && !StringUtil.isEmpty(element.getIcon().getLargIcon())) {
						descrLarge = ImageDescriptor.createFromURL(element.getIcon().getLargeIconURL());
					}
					if (element.getIcon() != null && !StringUtil.isEmpty(element.getIcon().getSmallIcon())) {
						descrSmall = ImageDescriptor.createFromURL(element.getIcon().getSmallIconURL());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				PaletteEntry entry = new PaletteTemplateEntry(element.getDisplayName(), element.getDescription(), component, descrSmall, descrLarge);
				drawer.acceptsType(entry);
				drawer.add(entry);
			}
		}
		return root;
	}
}
