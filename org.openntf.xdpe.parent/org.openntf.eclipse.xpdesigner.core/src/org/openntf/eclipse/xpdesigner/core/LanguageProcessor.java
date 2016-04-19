package org.openntf.eclipse.xpdesigner.core;

import java.util.Locale;
import java.util.ResourceBundle;

import org.w3c.dom.Element;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.registry.parse.RegistryAnnotaterInfo;

public enum LanguageProcessor {
	INSTANCE;

	private static final String LANG = "lang";
	private static final String HTTP_WWW_W3_ORG_XML_1998_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
	private final String LOCAL_LANG = Locale.getDefault().getLanguage();

	public String getLocalizedValue(String value, RegistryAnnotaterInfo registryAnnotaterInfo, Element child) {
		if (checkLanguageFail(child)) {
			return null;
		}
		if (checkLangProp(value)) {
			String prop = value.substring(1, value.length() -1);
			ResourceBundle bundle = registryAnnotaterInfo.getResourceBundle();
			if (!bundle.containsKey(prop)) {
				return value;
			} else {
				return bundle.getString(prop);
			}
			
		}
		return value;
	}

	private boolean checkLangProp(String value) {
		return value.startsWith("%") && value.endsWith("%");

	}

	private boolean checkLanguageFail(Element child) {
		String lang = child.getAttributeNS(HTTP_WWW_W3_ORG_XML_1998_NAMESPACE, LANG);
		if (StringUtil.isEmpty(lang)) {
			return false;
		}
		return !LOCAL_LANG.equals(lang);
	}

}
