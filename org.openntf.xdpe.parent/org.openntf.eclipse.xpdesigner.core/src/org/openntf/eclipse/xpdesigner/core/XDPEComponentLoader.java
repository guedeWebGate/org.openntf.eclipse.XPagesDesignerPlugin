package org.openntf.eclipse.xpdesigner.core;

import java.net.MalformedURLException;
import java.util.List;

import org.eclipse.gef.palette.PaletteRoot;
import org.openntf.eclipse.xpdesigner.core.xdecomponents.XDELibrary;

public interface XDPEComponentLoader {

	List<XDELibrary> scanPlugins4XSPLibraries();

	PaletteRoot buildPaletteRoot() throws MalformedURLException;

}