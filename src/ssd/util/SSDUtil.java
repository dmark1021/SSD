package ssd.util;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class SSDUtil {
	private static IWorkbenchWindow window ;
	
	
	public static void print(String x) {
		window=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MessageDialog.openInformation(
			window.getShell(),
			"SSD",
			x);
		System.out.println(x);
	}
}
