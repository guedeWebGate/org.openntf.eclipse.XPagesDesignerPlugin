package org.openntf.eclipse.xpdesigner.core.xdecomponents;

import org.openntf.eclipse.xpdesigner.core.xspcomponents.extensionsfactory.ComponentCreator;
import org.w3c.dom.Element;

import com.ibm.xsp.registry.FacesExtensibleNode;
import com.ibm.xsp.registry.parse.RegistryAnnotaterInfo;

public enum ExtensionCreatorFactory {
	COMPONENT("component", "designerComponent", new ComponentCreator());
	private final String m_ElementName;
	private final String m_ExtensionName;
	private final IXDEExtensionCreator m_Creator;

	private ExtensionCreatorFactory(String elementName, String extensionName, IXDEExtensionCreator creator) {
		m_ElementName = elementName;
		m_ExtensionName = extensionName;
		m_Creator = creator;
	}

	public boolean isMyElement(String element) {
		return m_ElementName.equals(element);
	}

	public boolean processAnnotater(RegistryAnnotaterInfo registryAnnotaterInfo, FacesExtensibleNode node, Element element) {
		IXDEExtension extension = m_Creator.createXspExtension(registryAnnotaterInfo, node, element);
		if (extension != null) {
			node.setExtension(m_ExtensionName, extension);
			return true;
		}
		return false;
	}

	public IXDEExtension getExtensionFromNode(FacesExtensibleNode node) {
		return (IXDEExtension) node.getExtension(m_ExtensionName);
	}

	public static ExtensionCreatorFactory getFactoryForElementByName(String elementName) {
		for (ExtensionCreatorFactory factory : values()) {
			if (factory.isMyElement(elementName)) {
				return factory;
			}
		}
		return null;
	}

}
