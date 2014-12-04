package ssd;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "SSD"; //$NON-NLS-1$
	public static final String SSD_MARKER_TYPE = "SSD.SSDMarker";

	// The shared instance
	private static Activator plugin;
	//set the project using project variable
	private static IJavaProject project;
	private static IResource resource;
	private static boolean readyForReconcile=false;
	
	/**
	 * @return the readyForReconcile
	 */
	public static boolean isReadyForReconcile() {
		return readyForReconcile;
	}

	/**
	 * @param readyForReconcile the readyForReconcile to set
	 */
	public static void setReadyForReconcile(boolean readyForReconcile) {
		Activator.readyForReconcile = readyForReconcile;
	}

	/**
	 * @return the resource
	 */
	public static IResource getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public static void setResource(IResource resource) {
		Activator.resource = resource;
	}

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	public static IJavaProject getProject() {
		return project;
	}

	public static void setProject(IJavaProject project) {
		if(Activator.project==null){
		Activator.project = project;}
	}
	public static void clearProject() {
Activator.project=null;
	}

}
