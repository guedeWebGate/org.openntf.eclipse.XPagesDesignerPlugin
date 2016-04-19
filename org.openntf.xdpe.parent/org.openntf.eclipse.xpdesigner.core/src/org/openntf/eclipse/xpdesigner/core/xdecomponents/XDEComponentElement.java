package org.openntf.eclipse.xpdesigner.core.xdecomponents;

public class XDEComponentElement extends XDEBasicElement {

	private String m_Cateogry;
	private String m_SelectedEvent;
	private boolean m_Visible = true;
	private boolean m_InPalette = false;
	private boolean m_GenerateId = true;
	private String m_RenderMarkup;
	private boolean m_Deprecated = false;

	public boolean hasNoCategory() {
		return m_Cateogry == null;
	}

	public String getCateogry() {
		return m_Cateogry;
	}

	public void setCateogry(String cateogry) {
		m_Cateogry = cateogry;
	}

	public boolean hasNoSelectedEvent() {
		return m_SelectedEvent == null;
	}

	public String getSelectedEvent() {
		return m_SelectedEvent;
	}

	public void setSelectedEvent(String selectedEvent) {
		m_SelectedEvent = selectedEvent;
	}

	public boolean isVisible() {
		return m_Visible;
	}

	public void setVisible(boolean visible) {
		m_Visible = visible;
	}

	public boolean isInPalette() {
		return m_InPalette;
	}

	public void setInPalette(boolean inPalette) {
		m_InPalette = inPalette;
	}

	public boolean isGenerateId() {
		return m_GenerateId;
	}

	public void setGenerateId(boolean generateId) {
		m_GenerateId = generateId;
	}

	public boolean hasNoRenderMarkup() {
		return m_RenderMarkup == null;
	}

	public String getRenderMarkup() {
		return m_RenderMarkup;
	}

	public void setRenderMarkup(String renderMarkup) {
		m_RenderMarkup = renderMarkup;
	}

	public boolean isDeprecated() {
		return m_Deprecated;
	}

	public void setDeprecated(boolean deprecated) {
		m_Deprecated = deprecated;
	}

}
