package org.openntf.eclipse.xpdesigner.ui.views;

import java.util.List;
import java.util.Map;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteTemplateEntry;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.views.palette.PaletteView;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.openntf.eclipse.xpdesigner.core.XPagesComponentProvider;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.ExtensionCreatorFactory;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDEComponentElement;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.registry.FacesComponentDefinition;

public class PaletteViewController extends PaletteView {

	public PaletteViewController() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		EditDomain editDomain = buildEditDomain();
		PaletteViewerProvider provider = new PaletteViewerProvider(editDomain);
		provider.createPaletteViewer(parent);
	}

	private EditDomain buildEditDomain() {
		EditDomain domain = new EditDomain();
		domain.setPaletteRoot(buildRoot());
		return domain;
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
