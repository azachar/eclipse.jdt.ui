package org.eclipse.jdt.internal.corext.refactoring.rename;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.refactoring.Assert;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringCoreMessages;

import org.eclipse.jdt.internal.corext.refactoring.base.IChange;
import org.eclipse.jdt.internal.corext.refactoring.base.Refactoring;
import org.eclipse.jdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.jdt.internal.corext.refactoring.changes.RenameJavaProjectChange;
import org.eclipse.jdt.internal.corext.refactoring.tagging.IReferenceUpdatingRefactoring;
import org.eclipse.jdt.internal.corext.refactoring.tagging.IRenameRefactoring;
import org.eclipse.jdt.internal.ui.JavaPlugin;


public class RenameJavaProjectRefactoring extends Refactoring implements IRenameRefactoring, IReferenceUpdatingRefactoring {

	private final IJavaProject fProject;
	private String fNewName;
	private boolean fUpdateReferences;
	
	public RenameJavaProjectRefactoring(IJavaProject project){
		Assert.isNotNull(project); 
		fProject= project;
		fNewName= project.getElementName();
		fUpdateReferences= false;
	}
	
	public Object getNewElement() throws JavaModelException{
		IPath newPath= fProject.getCorrespondingResource().getFullPath().removeLastSegments(1).append(getNewName());
		return ResourcesPlugin.getWorkspace().getRoot().findMember(newPath);
	}
	
	/* non java-doc
	 * @see IRenameRefactoring#setNewName(String)
	 */
	public void setNewName(String newName) {
		Assert.isNotNull(newName);
		fNewName= newName;
	}
	
	/* non java-doc
	 * @see IRenameRefactoring#getNewName()
	*/
	public String getNewName(){
		return fNewName;
	}

	/* non java-doc
	 * @see IRenameRefactoring#getCurrentName()
	 */
	public String getCurrentName() {
		return fProject.getElementName();
	}
	
	/* non java-doc
	 * @see IRefactoring#getName()
	 */
	public String getName() {
		String messages= RefactoringCoreMessages.getFormattedString("RenameJavaProjectRefactoring.rename", //$NON-NLS-1$
							new String[]{getCurrentName(), fNewName});
		return messages;
	}
	
	/*
	 * @see IReferenceUpdatingRefactoring#canEnableUpdateReferences()
	 */
	public boolean canEnableUpdateReferences() {
		return true;
	}

	/*
	 * @see IReferenceUpdatingRefactoring#setUpdateReferences(boolean)
	 */
	public void setUpdateReferences(boolean update) {
		fUpdateReferences= update;
	}

	/*
	 * @see IReferenceUpdatingRefactoring#getUpdateReferences()
	 */
	public boolean getUpdateReferences() {
		return fUpdateReferences;
	}
	
		
	//-- preconditions
	
	/* non java-doc
	 * @see Refactoring#checkActivation(IProgressMonitor)
	 */
	public RefactoringStatus checkActivation(IProgressMonitor pm) throws JavaModelException {
		if (! fProject.exists())
			return RefactoringStatus.createFatalErrorStatus(""); //$NON-NLS-1$
		
		if (fProject.isReadOnly())
			return RefactoringStatus.createFatalErrorStatus(""); //$NON-NLS-1$
		
		if (! fProject.isConsistent())
			return RefactoringStatus.createFatalErrorStatus(""); //$NON-NLS-1$
		
		if (! fProject.isStructureKnown())
			return RefactoringStatus.createFatalErrorStatus(""); //$NON-NLS-1$
		
		return new RefactoringStatus();
	}
	
	/* non java-doc
	 * @see IRenameRefactoring#checkNewName()
	 */
	public RefactoringStatus checkNewName(String newName) throws JavaModelException {
		Assert.isNotNull(newName, "new name"); //$NON-NLS-1$
		RefactoringStatus result= RefactoringStatus.create(JavaPlugin.getWorkspace().validateName(newName, IResource.PROJECT));
		if (result.hasFatalError())
			return result;
		
		if (projectNameAlreadyExists(newName))
			return RefactoringStatus.createFatalErrorStatus(RefactoringCoreMessages.getString("RenameJavaProjectRefactoring.already_exists")); //$NON-NLS-1$
		
		return new RefactoringStatus();
	}
	
	
	/* non java-doc
	 * @see Refactoring#checkInput(IProgressMonitor)
	 */
	public RefactoringStatus checkInput(IProgressMonitor pm) throws JavaModelException {
		pm.beginTask("", 1); //$NON-NLS-1$
		try{
			if (isReadOnly()){
				String message= RefactoringCoreMessages.getFormattedString("RenameJavaProjectRefactoring.read_only", //$NON-NLS-1$
									fProject.getElementName());
				return RefactoringStatus.createErrorStatus(message);
			}	
			return new RefactoringStatus();
		} finally{
			pm.done();
		}	
	}
	
	private boolean isReadOnly() throws JavaModelException{
		return fProject.getCorrespondingResource().isReadOnly();
	}
	
	private boolean projectNameAlreadyExists(String newName){
		return fProject.getJavaModel().getJavaProject(newName).exists();
	}

	//--- changes 
	
	/* non java-doc
	 * @see IRefactoring#createChange(IProgressMonitor)
	 */
	public IChange createChange(IProgressMonitor pm) throws JavaModelException {
		pm.beginTask("", 1); //$NON-NLS-1$
		try{
			return new RenameJavaProjectChange(fProject, fNewName, fUpdateReferences);
		} finally{
			pm.done();
		}	
	}
}