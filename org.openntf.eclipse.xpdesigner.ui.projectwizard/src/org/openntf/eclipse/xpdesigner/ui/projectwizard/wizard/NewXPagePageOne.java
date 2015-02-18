package org.openntf.eclipse.xpdesigner.ui.projectwizard.wizard;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openntf.eclipse.xpdesigner.ui.natures.ProjectNature;

public class NewXPagePageOne extends WizardPage {

	private static final String REGEX_PATTERN = "^[A-Za-z0-9_]+$";

	private Text m_Filename;
	private Text m_Comment;
	private Combo m_Projects;
	private IStructuredSelection m_Selection;
	private Map<String, IProject> m_ProjectList;
	private final String m_Target;

	protected NewXPagePageOne(String pageName, IStructuredSelection selection, String target) {
		super(pageName);
		m_Selection = selection;
		m_Target = target;

	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout gdl = new GridLayout(2, false);
		comp.setLayout(gdl);

		GridData gd = new GridData();
		gd.widthHint = 100;

		Label lab = new Label(comp, SWT.NONE);
		lab.setText("Name:");
		lab.setLayoutData(gd);
		m_Filename = new Text(comp, SWT.BORDER);
		m_Filename.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_Filename.setLayoutData(gd);

		Label lab2 = new Label(comp, SWT.NONE);
		lab2.setText("Comment:");

		m_Comment = new Text(comp, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		m_Comment.setLayoutData(gd);
		Label lab3 = new Label(comp, SWT.NONE);
		lab3.setText("XPage Project:");
		m_Projects = new Combo(comp, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		buildProjectList();
		List<String> projectNames = getProjectsAsList();
		String selectedProject = getInitProject();
		m_Projects.setItems(projectNames.toArray(new String[projectNames.size()]));
		if (!"".equals(selectedProject) && projectNames.indexOf(selectedProject) > -1) {
			m_Projects.select(projectNames.indexOf(selectedProject));
		} else {
			if (!"".equals(selectedProject)) {
				setMessage(selectedProject + " is not a XPages project, please select a XPages project", DialogPage.WARNING);
			}
			m_Projects.select(0);
		}
		m_Projects.setLayoutData(gd);

		setControl(comp);
		setPageComplete(false);
	}

	private void buildProjectList() {
		m_ProjectList = new TreeMap<String, IProject>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			try {
				if (project.hasNature(ProjectNature.NATURE_ID)) {
					m_ProjectList.put(project.getName(), project);
				}
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}
	}

	private List<String> getProjectsAsList() {
		List<String> projectNames = new LinkedList<String>();
		projectNames.addAll(m_ProjectList.keySet());
		return projectNames;
	}

	private String getInitProject() {
		IResource resource = null;
		if (m_Selection.getFirstElement() instanceof IResource) {
			resource = (IResource) m_Selection.getFirstElement();
		}
		if (resource == null && m_Selection.getFirstElement() instanceof IAdaptable) {
			IAdaptable ad = (IAdaptable) m_Selection.getFirstElement();
			resource = (IResource) ad.getAdapter(IResource.class);
		}
		if (resource != null) {
			IProject project = resource.getProject();
			return project.getName();
		}
		return "";
	}

	public Text getFilename() {
		return m_Filename;
	}

	public void setFilename(Text filename) {
		m_Filename = filename;
	}

	public Text getComment() {
		return m_Comment;
	}

	public void setComment(Text comment) {
		m_Comment = comment;
	}

	public IProject getSelectedProject() {
		return m_ProjectList.get(m_Projects.getItem(m_Projects.getSelectionIndex()));
	}

	private void validate() {
		if (!m_Filename.getText().matches(REGEX_PATTERN)) {
			setMessage("A name must start with a letter or underscore (_) and can only contain numbers, letters and underscores (_).", DialogPage.ERROR);
			setPageComplete(false);
			return;
		}
		if (fileNameExist()) {
			setMessage("A file with this name already exists as xpages or custom control.", DialogPage.ERROR);
			setPageComplete(false);
			return;

		}
		if ("".equals(m_Filename.getText().trim())) {
			setMessage("Create a new " + m_Target + ".", DialogPage.INFORMATION);
			setPageComplete(false);
			return;
		}
		setMessage("Create a new " + m_Target + ".", DialogPage.INFORMATION);
		setPageComplete(true);

	}

	private boolean fileNameExist() {
		IProject project = getSelectedProject();
		IFile fileXSP = project.getFile("XPages/" + m_Filename.getText() + ".xsp");
		IFile fileCC = project.getFile("CustomControls/" + m_Filename.getText() + ".xsp");
		return fileXSP.exists() || fileCC.exists();
	}
}
