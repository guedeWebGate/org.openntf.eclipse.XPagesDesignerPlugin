package org.openntf.eclipse.xpdesigner.core;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.core.target.ITargetDefinition;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.openntf.eclipse.xpdesigner.core.loaders.TargetBundleClassLoader;
import org.osgi.framework.BundleContext;

public class CoreActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.openntf.eclipse.xpdesigner.core";

	private static final String[] XSP_CL_IDS = { "org.eclipse.core.jobs", "org.eclipse.equinox.common", "org.eclipse.core.resources", PLUGIN_ID, "com.ibm.xsp.core", "com.ibm.xsp.designer" };

	private static CoreActivator coreActivator;

	private ClassLoader customClassLoader;
	private BundleContext context;
	private MessageConsoleStream consoleLog;
	private String tempDir;
	@Override
	public void start(BundleContext arg0) throws Exception {
		coreActivator = this;
		context = arg0;
		MessageConsole console = findConsole("XDPE -Console Core");
		consoleLog = console.newMessageStream();
		tempDir = getTempSubDir();


	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		consoleLog.close();
		coreActivator = null;
		context = null;

	}

	public static CoreActivator getDefault() {
		return coreActivator;
	}
	
	public BundleContext getBundleContext() {
		return context;
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	public void log(String message) {
		consoleLog.println(message);
	}

	public void logException(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		log(sw.toString());
	}
	
	private String getTempSubDir() {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss");
		String subDir = "XDPE_" + sdf.format(new Date());
		String path = System.getProperty("java.io.tmpdir") + File.separator + subDir;
		File tempDirFile = new File(path);
		tempDirFile.mkdirs();
		return path;
	}

	
	public String getNewTempSubDir(String subDir) {
		String path = tempDir + File.separator + subDir;
		File tempDirFile = new File(path);
		tempDirFile.mkdirs();
		return path;
	}

	public synchronized ClassLoader getXSPClassLoader() throws MalformedURLException {
		if (customClassLoader == null) {
			customClassLoader = buildBaseClassLoader(Arrays.asList(XSP_CL_IDS));
		}
		return customClassLoader;
	}


	public synchronized ClassLoader buildBaseClassLoader(List<String> pluginIds) throws MalformedURLException {
		log("Start building TargetBundleClassLoader: " + new Date());
		IPluginModelBase[] base = PluginRegistry.getActiveModels(false);
		ITargetDefinition activeTargetDefinition = TargetPlatformBuilder.INSTANCE.getActiveTargetDefinition();
		Set<String> allDependencies = new HashSet<String>();
		for (IPluginModelBase element : base) {
			IPluginBase pbase = element.getPluginBase();
			if (pluginIds.contains(pbase.getId())) {
				log("Loading Dependencies for " + pbase.getId());
				Set<String> dependecies = buildDependencies(pbase.getPluginModel(), pbase.getId());
				allDependencies.addAll(dependecies);
			}
		}
		if (!activeTargetDefinition.isResolved()) {
			activeTargetDefinition.resolve(null);
		}
		List<TargetBundle> tBundles = collectTargetBundles(activeTargetDefinition, allDependencies);
		List<URI> allBundles = buildURIListFromTargetBundles(tBundles);
		log("All bundles has " + allBundles.size() + " entries.");
		TargetBundleClassLoader targetBundleCL = new TargetBundleClassLoader(allBundles, this.getClass().getClassLoader(), CoreActivator.PLUGIN_ID + "-mainCL");
		return targetBundleCL;
	}

	private List<URI> buildURIListFromTargetBundles(List<TargetBundle> tBundles) {
		List<URI> allBundles = new ArrayList<URI>();
		for (TargetBundle tbundle : tBundles) {
			allBundles.add(tbundle.getBundleInfo().getLocation());
		}
		return allBundles;
	}
	private Set<String> buildDependencies(IPluginModelBase model, String id) {
		Set<String> depend = new HashSet<String>();
		depend.add(id);
		addDependencies(model.getBundleDescription().getRequiredBundles(), depend);
		return depend;
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
	
	private void addDependencies(BundleSpecification[] bundlesSpecs, Set<String> depend) {
		if (bundlesSpecs == null) {
			return;
		}
		for (BundleSpecification spec : bundlesSpecs) {
			String name = spec.getName();
			if (!depend.contains(name) && !spec.isOptional()) {
				depend.add(name);
			}
		}
	}


}
