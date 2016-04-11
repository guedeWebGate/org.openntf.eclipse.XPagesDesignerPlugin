package org.openntf.eclipse.xpdesigner.core.xspcomponents.extensionsfactory;

import java.net.URL;
import java.util.Iterator;

import org.openntf.eclipse.xpdesigner.core.LanguageProcessor;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.IXDEExtension;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.IXDEExtensionCreator;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDEBasicElement;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDEIcon;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.registry.FacesExtensibleNode;
import com.ibm.xsp.registry.parse.ElementUtil;
import com.ibm.xsp.registry.parse.RegistryAnnotaterInfo;

public abstract class AbstractCreator implements IXDEExtensionCreator {

	public AbstractCreator() {
		super();
	}

	@Override
	public IXDEExtension createXspExtension(RegistryAnnotaterInfo registryAnnotaterInfo, FacesExtensibleNode node, Element element) {

		XDEBasicElement extElement = createXDEElement();
		applyBasicInformations(extElement, registryAnnotaterInfo, element);
		applyExtensionInformation(extElement, registryAnnotaterInfo, element);
		return extElement;
	}

	protected abstract void applyExtensionInformation(XDEBasicElement extElement, RegistryAnnotaterInfo registryAnnotaterInfo, Element element);

	protected abstract XDEBasicElement createXDEElement();

	protected void applyBasicInformations(XDEBasicElement extElement, RegistryAnnotaterInfo registryAnnotaterInfo, Element element) {

		for (Iterator<Element> iterator = ElementUtil.getChildren(element).iterator(); iterator.hasNext();) {
			Element child = iterator.next();
			String tagName = child.getTagName();
			if ("display-name".equals(tagName)) {
				if (extElement.hasNoDisplayName()) {
					String value = getStringValue(child);
					String langValue = LanguageProcessor.INSTANCE.getLocalizedValue(value, registryAnnotaterInfo, child);
					if (!StringUtil.isEmpty(langValue)) {
						extElement.setDisplayName(langValue);
					}
				}
			}
			if ("description".equals(tagName)) {
				if (extElement.hasNoDescription()) {
					String value = getStringValue(child);
					String langValue = LanguageProcessor.INSTANCE.getLocalizedValue(value, registryAnnotaterInfo, child);
					if (!StringUtil.isEmpty(langValue)) {
						extElement.setDescription(langValue);
					}
				}
			}
			if ("icon".equals(tagName)) {
				if (extElement.hasNoIcon()) {
					XDEIcon icon = buildIcon(child, registryAnnotaterInfo);
					if (icon != null) {
						extElement.setIcon(icon);
					}
				}

			}
		}

	}

	private XDEIcon buildIcon(Element child, RegistryAnnotaterInfo registryAnnotaterInfo) {
		String smallIcon = getChildNodeStringValue(child, "small-icon");
		String largeIcon = getChildNodeStringValue(child, "large-icon");
		URL smallIconURL = buildIconURL(smallIcon, registryAnnotaterInfo);
		URL largeIconURL = buildIconURL(largeIcon, registryAnnotaterInfo);
		return new XDEIcon(smallIcon, largeIcon, smallIconURL, largeIconURL);
	}

	private URL buildIconURL(String iconValue, RegistryAnnotaterInfo registryAnnotaterInfo) {
		if (StringUtil.isEmpty(iconValue)) {
			return null;
		}
		return registryAnnotaterInfo.getDesignIconUrl(iconValue);
	}

	public String getStringValue(Element element) {
		StringBuilder sb = new StringBuilder();
		scanChildren(sb, element);
		return sb.toString();
	}

	private void scanChildren(StringBuilder sb, Element element) {
		NodeList nodes = element.getChildNodes();
		for (int index = 0; index < nodes.getLength(); index++) {
			Node node = nodes.item(index);
			if (node instanceof Text || node instanceof CDATASection) {
				sb.append(((CharacterData) node).getData());
			}
			if (node instanceof Element) {
				scanChildren(sb, (Element) node);
			}
		}

	}

	private String getChildNodeStringValue(Element element, String childNodeName) {
		Element firstElement = ElementUtil.getFirstChildElement(element, childNodeName);
		if (firstElement != null) {
			return ElementUtil.getValue(firstElement);
		}
		return null;
	}
}