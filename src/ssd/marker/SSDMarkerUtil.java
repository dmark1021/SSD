package ssd.marker;

import ssd.Activator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;

public class SSDMarkerUtil {
	public static IMarker addMarker(CompilationUnit compilationUnit,
			Map<String, Object> markerAttributes) {

		IMarker marker = null;

		try {

			IJavaElement javaElement = compilationUnit.getJavaElement();
			if (javaElement != null) {
				IFile file = (IFile) javaElement.getCorrespondingResource();
				marker = file.createMarker(Activator.SSD_MARKER_TYPE);
				marker.setAttributes(markerAttributes);
			}

		} catch (CoreException e) {
		}

		return marker;
	}
	public static IMarker addSSDMarker(String varname, String msg,
			ASTNode node) {

		IMarker marker = null;
		CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(node);

		try {

			IJavaElement javaElement = astRoot.getJavaElement();
			if (javaElement != null) {
				IFile file = (IFile) javaElement.getCorrespondingResource();
				marker = file.createMarker(Activator.SSD_MARKER_TYPE);
				Map<String, Object> markerAttributes = new HashMap<String, Object>();
				int lineNumber = astRoot.getLineNumber(node.getStartPosition());
				markerAttributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				markerAttributes.put(IMarker.LINE_NUMBER, lineNumber);
				markerAttributes.put(IMarker.MESSAGE,msg);
				marker.setAttributes(markerAttributes);
			}

		} catch (CoreException e) {
		}

		return marker;
	}

	public static void clearStaleMarkers(ArrayList<IMarker> markers) {
		try {
			for (IMarker marker : markers) {
				if (marker != null && marker.exists()) {
					marker.delete();
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	public static void clearStaleMarkers(IMarker[] markers) {
		try {
			for (IMarker marker : markers) {
				if (marker != null && marker.exists()) {
					marker.delete();
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}


}
