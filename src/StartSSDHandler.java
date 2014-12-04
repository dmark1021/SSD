import ssd.analysis.SSDMain;
import ssd.marker.SSDMarkerUtil;
import ssd.util.SSDUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import ssd.Activator;


public class StartSSDHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IResource resource = null;
		if(Activator.getProject()==null){
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		ISelection selection=window.getSelectionService().getSelection();
		resource=extractSelection(selection);
				IProject project = null;
				IJavaProject javaProject = null;
					project = resource.getProject();
					javaProject = JavaCore.create(project);
				if (project != null && javaProject != null) {
					
					Activator.setProject(javaProject);
					Activator.setResource(resource);
				}}
		else{
			SSDUtil.print("Currently"+Activator.getProject().toString()+"project is set. Please clear it first");	
		}
		/**
		 * clear existing markers
		 */
		IMarker[] markers=findSSDMarkers(resource);
		SSDMarkerUtil.clearStaleMarkers(markers);
		SSDMain SSDmain=new SSDMain();
		SSDmain.runStartingAnalysis();
		Activator.setReadyForReconcile(true);
		return null;
	}
	
	
	//will return currently selected Iresource
	 IResource extractSelection(ISelection sel) {
	      if (!(sel instanceof IStructuredSelection))
	         return null;
	      IStructuredSelection ss = (IStructuredSelection) sel;
	      Object element = ss.getFirstElement();
	      if (element instanceof IResource)
	         return (IResource) element;
	      if (!(element instanceof IAdaptable))
	         return null;
	      IAdaptable adaptable = (IAdaptable)element;
	      Object adapter = adaptable.getAdapter(IResource.class);
	      return (IResource) adapter;
	   }
	 public IMarker[] findSSDMarkers(IResource target) {
		   String type = Activator.SSD_MARKER_TYPE;
		   IMarker[] markers = null;
		   try {
			markers = target.findMarkers(type, true, IResource.DEPTH_INFINITE);
			return markers;
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return markers;
		   
		}

}
