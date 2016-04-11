package org.openntf.eclipse.xpdesigner.core;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CoreActivator implements BundleActivator {

	private static CoreActivator coreActivator;

	private ClassLoader customClassLoader;
	private BundleContext context;
	private MessageConsoleStream consoleLog;

	@Override
	public void start(BundleContext arg0) throws Exception {
		coreActivator = this;
		context = arg0;
		MessageConsole console = findConsole("XDPE -Console");
		consoleLog = console.newMessageStream();

	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		consoleLog.close();
		coreActivator = null;
		context = null;

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

}
