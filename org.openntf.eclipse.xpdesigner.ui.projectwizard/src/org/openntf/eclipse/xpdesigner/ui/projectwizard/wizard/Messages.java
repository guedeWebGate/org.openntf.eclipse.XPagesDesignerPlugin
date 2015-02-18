package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard.messages"; //$NON-NLS-1$
	public static String NewXPProject_WINDOW_TITLE;
	public static String NewXPProject_WIZARD_DESCRIPTION;
	public static String NewXPProject_WIZARD_NAME;
	public static String NewXPProject_WIZARD_TITLE;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
