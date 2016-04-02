package org.openntf.eclipse.xpdesigner.core.xdecomponents;

import org.w3c.dom.Element;

import com.ibm.xsp.registry.FacesExtensibleNode;
import com.ibm.xsp.registry.parse.RegistryAnnotater;
import com.ibm.xsp.registry.parse.RegistryAnnotaterInfo;

public class ComponentAnnotater implements RegistryAnnotater {

	@Override
	public void annotate(RegistryAnnotaterInfo registryAnnotaterInfo, FacesExtensibleNode node, Element element) {
		ExtensionCreatorFactory factory = ExtensionCreatorFactory.getFactoryForElementByName(element.getLocalName());
		if (factory != null) {
			factory.processAnnotater(registryAnnotaterInfo, node, element);
		}

	}

}
