package org.openntf.eclipse.xpdesigner.core.xspcomponents.extensionsfactory;

import java.util.Iterator;

import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDEBasicElement;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDEComponentElement;
import org.w3c.dom.Element;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.page.parse.FacesReader;
import com.ibm.xsp.page.parse.FacesReader.ReaderContent;
import com.ibm.xsp.registry.parse.ElementUtil;
import com.ibm.xsp.registry.parse.ParseUtil;
import com.ibm.xsp.registry.parse.RegistryAnnotaterInfo;

public class ComponentCreator extends AbstractCreator {

	protected XDEBasicElement createXDEElement() {
		return new XDEComponentElement();
	}

	protected void applyExtensionInformation(XDEBasicElement extElement, RegistryAnnotaterInfo registryAnnotaterInfo, Element element) {
		XDEComponentElement component = (XDEComponentElement) extElement;
		for (Iterator<Element> iterator = ElementUtil.getExtensions(element, "component-extension").iterator(); iterator.hasNext();) {
			Element extension = iterator.next();
			if ("designer-extension".equals(extension.getTagName())) {
				for (Element child : ElementUtil.getChildren(extension)) {
					String tagName = child.getTagName();

					if ("category".equals(tagName)) {
						if (component.hasNoCategory()) {
							String value = ElementUtil.getValue(child);
							if (!StringUtil.isEmpty(value)) {
								component.setCateogry(value);
							}
						}
					}
					if ("selected-event".equals(tagName)) {
						if (component.hasNoSelectedEvent()) {
							String value = ElementUtil.getValue(child);
							if (!StringUtil.isEmpty(value)) {
								component.setSelectedEvent(value);
							}
						}
					}
					if ("visible".equals(tagName)) {
						String value = ElementUtil.getValue(child);
						boolean result = ParseUtil.getBoolean(value, component.isVisible());
						component.setVisible(result);
					}
					if ("render-markup".equals(tagName)) {
						Iterator<ReaderContent> it = new FacesReader.Tag(child).getChildren();
						if (it.hasNext()) {
							FacesReader.ReaderContent content = it.next();
							if (!it.hasNext() && ReaderContent.TYPE_TEXT == content.getType()) {
								String markup = ((FacesReader.TextContent) content).getText();
								if (!StringUtil.isEmpty(markup)) {
									component.setRenderMarkup(markup);
								}
							}
						}
					}
					if ("in-palette".equals(tagName)) {
						String value = ElementUtil.getValue(child);
						boolean result = ParseUtil.getBoolean(value, component.isInPalette());
						component.setInPalette(result);
					}
					if ("generate-id".equals(tagName)) {
						String value = ElementUtil.getValue(child);
						boolean result = ParseUtil.getBoolean(value, component.isGenerateId());
						component.setGenerateId(result);
					}
					if ("is-deprecated".equals(tagName)) {
						String value = ElementUtil.getValue(child);
						boolean result = ParseUtil.getBoolean(value, component.isDeprecated());
						component.setDeprecated(result);
					}
				}
			}
		}
	}
}
