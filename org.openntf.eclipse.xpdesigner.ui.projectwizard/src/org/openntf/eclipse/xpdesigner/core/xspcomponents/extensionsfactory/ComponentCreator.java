package org.openntf.eclipse.xpdesigner.core.xspcomponents.extensionsfactory;

import org.openntf.eclipse.xpdesigner.core.xspcomponents.XDEComponentElement;
import org.openntf.eclipse.xpdesigner.core.xspcomponents.XDEBasicElement;
import org.w3c.dom.Element;

import com.ibm.xsp.registry.parse.RegistryAnnotaterInfo;

public class ComponentCreator extends AbstractCreator {

	protected XDEBasicElement createXDEElement() {
		return new XDEComponentElement();
	}

	protected void applyExtensionInformation(XDEBasicElement extElement, RegistryAnnotaterInfo registryAnnotaterInfo, Element element) {
	}

}
