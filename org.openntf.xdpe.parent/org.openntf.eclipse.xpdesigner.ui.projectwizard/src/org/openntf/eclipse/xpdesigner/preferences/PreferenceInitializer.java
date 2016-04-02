package org.openntf.eclipse.xpdesigner.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.openntf.eclipse.xpdesigner.ui.projectwizard.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.PREF_DOMINO_P2_DIR, "");
		store.setDefault(PreferenceConstants.PREF_DOMINO_BIN_DIR, "");
		store.setDefault(PreferenceConstants.PREF_DOMINO_DATA_DIR,"");
	}

}
