package org.openntf.eclipse.xpdesigner.core.xdecomponents;

import org.w3c.dom.Element;

import com.ibm.xsp.registry.FacesExtensibleNode;
import com.ibm.xsp.registry.parse.RegistryAnnotaterInfo;

public interface IXDEExtensionCreator {

	public IXDEExtension createXspExtension(RegistryAnnotaterInfo registryAnnotaterInfo, FacesExtensibleNode node, Element element);
}
