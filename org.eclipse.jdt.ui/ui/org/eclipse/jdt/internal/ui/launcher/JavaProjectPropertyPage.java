/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.launcher;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.dialogs.ErrorDialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/*
 * The page for setting java runtime
 */
public abstract class JavaProjectPropertyPage extends PropertyPage {
	
	private boolean fIsCreated;
	
	public final Control createContents(Composite parent) {
		if (getJavaProject() == null) {
			return createNoJavaContents(parent);
		} else {
			if (isOpenProject())  {
				fIsCreated= true;
				return createJavaContents(parent);
			}
			return createClosedContents(parent);
		}
	}
	
	protected abstract boolean performJavaOk();
	protected abstract Control createJavaContents(Composite parent);
	
	protected Control createNoJavaContents(Composite parent) {
		return createLabelOnly(parent, LauncherMessages.getString("javaProjectPropertyPage.notJava")); //$NON-NLS-1$
	};
	
	protected Control createClosedContents(Composite parent) {
		return createLabelOnly(parent, LauncherMessages.getString("javaProjectPropertyPage.closed")); //$NON-NLS-1$
	}
	
	protected Control createLabelOnly(Composite parent, String labelText) {
		noDefaultAndApplyButton();
		Label label= new Label(parent, SWT.LEFT);
		label.setText(labelText);
		return label;
	}

	protected boolean isJavaProject(IProject proj) {
		try {
			return proj.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			ErrorDialog.openError(getControl().getShell(), LauncherMessages.getString("javaProjectPropertyPage.exception"), null, e.getStatus()); //$NON-NLS-1$
		}
		return false;
	}
	
	protected boolean isOpenProject() {
		IProject p= null;
		Object o= getElement();
		if (o instanceof IJavaProject)
			p= ((IJavaProject)o).getProject();
			
		if (o instanceof IProject) 
			p= (IProject)o;
		if (p != null)
			return p.isOpen();
		return false;
	}
	
	public boolean isCreated() {
		return fIsCreated;
	}
	
	final public boolean performOk() {
		if (isCreated()) {
			return performJavaOk();
		}
		return true;
	}
		
	protected IJavaProject getJavaProject() {
		Object o= getElement();
		if (o instanceof IProject) {
			IProject p= (IProject)o;
			if (isJavaProject(p))
				o= JavaCore.create((IProject)o);
		}
		if (o instanceof IJavaProject)
			return (IJavaProject)o;
		return null;
	}
}