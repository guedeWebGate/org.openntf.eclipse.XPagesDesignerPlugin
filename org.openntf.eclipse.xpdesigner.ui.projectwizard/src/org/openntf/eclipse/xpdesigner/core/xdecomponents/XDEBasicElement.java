package org.openntf.eclipse.xpdesigner.core.xdecomponents;

public class XDEBasicElement implements IXDEExtension {
	private String m_Description;
	private String m_DisplayName;
	private XDEIcon m_Icon;

	public boolean hasNoDisplayName() {
		return m_DisplayName == null;
	}

	public void setDisplayName(String langValue) {
		m_DisplayName = langValue;

	}

	public boolean hasNoDescription() {
		return m_Description == null;
	}

	public void setDescription(String langValue) {
		m_Description = langValue;

	}

	public boolean hasNoIcon() {
		return m_Icon == null;
	}

	public void setIcon(XDEIcon icon) {
		m_Icon = icon;

	}

	public String getDescription() {
		return m_Description;
	}

	public String getDisplayName() {
		return m_DisplayName;
	}

	public XDEIcon getIcon() {
		return m_Icon;
	}

}
