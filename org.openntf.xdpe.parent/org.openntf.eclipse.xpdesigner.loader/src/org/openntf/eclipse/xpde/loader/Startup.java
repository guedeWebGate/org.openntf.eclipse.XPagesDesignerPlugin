package org.openntf.eclipse.xpde.loader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.ITargetHandle;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.ui.IStartup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.FrameworkWiring;

//Class is not used, only to preserve the work

public class Startup implements IStartup {

	private BundleContext mainContext;
	private Bundle ibmCoreBundle;
	private List<String> logMessages = new ArrayList<String>();

	private static Startup startup;
	
	public static Startup getStartup() {
		return startup;
	}
	@Override
	public void earlyStartup() {
		mainContext = FrameworkUtil.getBundle(Platform.class).getBundleContext();
		bundleInstall(mainContext);
		startup = this;
	}

	private void bundleInstall(BundleContext context) {
		List<String> pluginIds = Arrays.asList("com.ibm.designer.lib.acf", "com.ibm.designer.lib.jsf", "com.ibm.pvc.servlet", "com.ibm.xsp.core");
		log("Start bundleInstaller: " + new Date());
		ITargetDefinition activeTargetDefinition = getActiveTargetDefinition(context);
		if (activeTargetDefinition == null) {
			log("TargetPlatform is not defined!");
			return;
		}
		Set<String> allDependencies = new HashSet<String>();
		for (String id : pluginIds) {
			allDependencies.add(id);
		}
		if (!activeTargetDefinition.isResolved()) {
			activeTargetDefinition.resolve(null);
		}
		List<TargetBundle> tBundles = collectTargetBundles(activeTargetDefinition, allDependencies);
		List<Bundle> loadedBundle = new ArrayList<Bundle>();
		for (TargetBundle bundle : tBundles) {
			try {

				Bundle bndl = context.installBundle(bundle.getBundleInfo().getLocation().toString());
				if (bndl.getState() == Bundle.INSTALLED) {
					loadedBundle.add(bndl);
				} else {
					log("ERROR: " + bndl.getSymbolicName() + " - STATE: " + bndl.getState() + " / " + Bundle.INSTALLED);
				}
			} catch (Exception e) {
				logException(e);
			}
		}

		refreshPackages(loadedBundle, context);
	}

	public void refreshPackages(List<Bundle> bundles, BundleContext context) {
		Bundle systemBundle = context.getBundle(0);
		Bundle myBundle = context.getBundle();
		for (Bundle bndl : bundles) {
			if ("com.ibm.xsp.core".equals(bndl.getSymbolicName())) {
				log("Starting: " + bndl);
				try {
					ibmCoreBundle = bndl;
					bndl.start();
				} catch (BundleException e) {
					logException(e);
				}
			}
		}
		log("MyBundle is:  " + myBundle);
		final boolean[] flag = new boolean[] { false };
		FrameworkListener listener = new FrameworkListener() {
			@Override
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED) {
					synchronized (flag) {
						log("DOING REFRESH: " + event.toString());
						flag[0] = true;
						flag.notifyAll();
					}
				}
			}
		};
		context.addFrameworkListener(listener);
		FrameworkWiring frameworkWiring = systemBundle.adapt(FrameworkWiring.class);
		frameworkWiring.refreshBundles(null);
		synchronized (flag) {
			while (!flag[0]) {
				try {
					flag.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		if (myBundle != null) {
			frameworkWiring.resolveBundles(Arrays.asList(myBundle));
		}
		context.removeFrameworkListener(listener);
		log("Refresh done...");
		try {
			Class<?> cl = ibmCoreBundle.loadClass("com.ibm.xsp.library.XspLibrary");
			log("Class is " + cl);
			log("Try to load com.ibm.xsp.registry.FacesRegistry");
			Class<?> clReg = ibmCoreBundle.loadClass("com.ibm.xsp.registry.FacesRegistry");
			log("Reg is " + clReg);
			log("Try to load class from MyBundle Core");
			Class<?> clCoreXSP = myBundle.loadClass("com.ibm.xsp.library.XspLibrary");
			log("Class is " + clCoreXSP);

		} catch (Exception ex) {
			logException(ex);
		}

	}

	public void log(String message) {
		logMessages.add(">>LOADER STARTUP: " + message);
	}

	public void logException(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		log(sw.toString());
	}

	private List<TargetBundle> collectTargetBundles(ITargetDefinition activeTargetDefinition, Set<String> dependecies) {
		List<TargetBundle> tBundles = new ArrayList<TargetBundle>();
		for (TargetBundle bundle : activeTargetDefinition.getBundles()) {
			String bundleId = bundle.getBundleInfo().getSymbolicName();
			if (dependecies.contains(bundleId)) {
				tBundles.add(bundle);
			}
		}
		return tBundles;
	}

	private ITargetDefinition getActiveTargetDefinition(BundleContext bc) {
		ServiceReference<ITargetPlatformService> ref = bc.getServiceReference(ITargetPlatformService.class);

		ITargetPlatformService tpService = bc.getService(ref);
		ITargetDefinition targetDef = null;
		try {
			for (ITargetHandle def : tpService.getTargets(null)) {
				if ("XPDE-Server".equals(def.getTargetDefinition().getName())) {
					targetDef = def.getTargetDefinition();
				}
			}
		} catch (Exception ex) {
			logException(ex);
		}
		return targetDef;

	}
	public List<String> getLogMessages() {
		return logMessages;
	}

}
