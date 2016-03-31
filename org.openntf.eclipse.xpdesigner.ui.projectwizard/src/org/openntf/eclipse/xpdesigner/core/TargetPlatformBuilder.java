package org.openntf.eclipse.xpdesigner.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.LoadTargetDefinitionJob;
import org.openntf.eclipse.xpdesigner.ui.projectwizard.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public enum TargetPlatformBuilder {
	INSTANCE;

	public void buildTargetPlatformP2AndServer(String p2Directory, String dataDirectory)
			throws CoreException, InterruptedException {
		BundleContext bc = Activator.getDefault().getBundle().getBundleContext();
		ServiceReference<ITargetPlatformService> ref = bc.getServiceReference(ITargetPlatformService.class);

		ITargetPlatformService tpService = bc.getService(ref);
		ITargetDefinition targetDef = null;
		for (ITargetHandle def : tpService.getTargets(null)) {
			if ("XPDE-Server".equals(def.getTargetDefinition().getName())) {
				targetDef = def.getTargetDefinition();
			}
		}
		if (targetDef == null) {
			targetDef = tpService.newTarget();
			targetDef.setName("XPDE-Server");
		}
		List<ITargetLocation> bundleContainers = new ArrayList<ITargetLocation>();
		bundleContainers.add(tpService.newDirectoryLocation("${eclipse_home}"));
		bundleContainers.add(tpService.newDirectoryLocation(
				p2Directory ));
		bundleContainers.add(tpService.newDirectoryLocation(dataDirectory + File.separator + "domino" + File.separator
				+ "workspace" + File.separator + "applications" + File.separator + "eclipse"));

		targetDef.setTargetLocations(bundleContainers.toArray(new ITargetLocation[bundleContainers.size()]));
		targetDef.setArch(Platform.getOSArch());
		targetDef.setOS(Platform.getOS());
		targetDef.setWS(Platform.getWS());
		targetDef.setNL(Platform.getNL());
		// targetDef.setJREContainer()
		tpService.saveTargetDefinition(targetDef);

		Job job = new LoadTargetDefinitionJob(targetDef);
		job.schedule();
		job.join();
	}
}
