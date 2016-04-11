package org.openntf.eclipse.xpdesigner.core.xdecomponents;

import java.net.URL;

public class XDEIcon {

	private final String m_SmallIcon;
	private final String m_LargIcon;
	private final URL m_SmallIconURL;
	private final URL m_LargeIconURL;

	public XDEIcon(String smallIcon, String largIcon, URL smallIconURL, URL largeIconURL) {
		super();
		m_SmallIcon = smallIcon;
		m_LargIcon = largIcon;
		m_SmallIconURL = smallIconURL;
		m_LargeIconURL = largeIconURL;
	}

	public String getSmallIcon() {
		return m_SmallIcon;
	}

	public String getLargIcon() {
		return m_LargIcon;
	}

	public URL getSmallIconURL() {
		return m_SmallIconURL;
	}

	public URL getLargeIconURL() {
		return m_LargeIconURL;
	}

}
