package org.openntf.eclipse.xpdesigner.ui.projectwizard;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {

	private IPageLayout factory;

	@Override
	public void createInitialLayout(IPageLayout factory) {
		this.factory = factory;
		addViews();
	}

	private void addViews() {
		IFolderLayout topLeft = factory.createFolder("topLeft", //$NON-NLS-1$
				IPageLayout.LEFT, 0.25f, factory.getEditorArea());
		topLeft.addView(JavaUI.ID_PACKAGES);
		topLeft.addPlaceholder(IPageLayout.ID_PROJECT_EXPLORER);
		topLeft.addPlaceholder(JavaUI.ID_TYPE_HIERARCHY);

		IFolderLayout bottom = factory.createFolder("bottomRight", //$NON-NLS-1$
				IPageLayout.BOTTOM, 0.75f, factory.getEditorArea());
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);

		IFolderLayout topRight = factory.createFolder("topRight", IPageLayout.RIGHT, 0.75f, factory.getEditorArea());
		topRight.addView(IPageLayout.ID_OUTLINE);
		topRight.addView("org.openntf.eclipse.xpdesigner.view.palette");

		factory.addNewWizardShortcut("org.openntf.eclipse.xpdesigner.ui.projectwizard.newxproject"); //$NON-NLS-1$
		factory.addNewWizardShortcut("org.openntf.eclipse.xpdesigner.ui.projectwizard.newXPages"); //$NON-NLS-1$
		factory.addNewWizardShortcut("org.openntf.eclipse.xpdesigner.ui.projectwizard.newcustomcontrol"); //$NON-NLS-1$
		factory.addNewWizardShortcut("org.openntf.eclipse.xpdesigner.ui.projectwizard.server");
		factory.addNewWizardShortcut("org.openntf.eclipse.xpdesigner.ui.projectwizard.application");
		//factory.addNewWizardShortcut("org.eclipse.pde.ui.NewFeatureProjectWizard"); //$NON-NLS-1$

	}

}
