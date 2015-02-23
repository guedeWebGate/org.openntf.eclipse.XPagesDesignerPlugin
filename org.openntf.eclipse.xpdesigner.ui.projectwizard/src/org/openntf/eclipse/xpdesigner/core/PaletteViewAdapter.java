package org.openntf.eclipse.xpdesigner.core;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.part.IContributedContentsView;

public class PaletteViewAdapter implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		System.out.println("PVA Called with:" + adaptableObject + " / " + adapterType);
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		// TODO Auto-generated method stub
		return new Class[] { IContributedContentsView.class };
	}

}
