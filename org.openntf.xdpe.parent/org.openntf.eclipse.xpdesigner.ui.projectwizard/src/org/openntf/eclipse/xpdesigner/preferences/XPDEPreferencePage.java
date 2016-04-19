package org.openntf.eclipse.xpdesigner.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.openntf.eclipse.xpdesigner.core.TargetPlatformBuilder;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.Activator;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class XPDEPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public XPDEPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("XPages Designer Setup and Integration");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.PREF_DOMINO_P2_DIR, "&Domino P2 UpdateSite directory:",
				getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.PREF_DOMINO_BIN_DIR, "&Domino Bin Directory:",
				getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.PREF_DOMINO_DATA_DIR, "&Domino Data Directory:",
				getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performOk() {
		String p2Path = getPreferenceStore().getString(PreferenceConstants.PREF_DOMINO_P2_DIR);
		//String progPath = getPreferenceStore().getString(PreferenceConstants.PREF_DOMINO_BIN_DIR);
		String dataPath = getPreferenceStore().getString(PreferenceConstants.PREF_DOMINO_DATA_DIR);
		try {
			TargetPlatformBuilder.INSTANCE.buildTargetPlatformP2AndServer(p2Path, dataPath);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return super.performOk();
	}

}