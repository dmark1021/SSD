import ssd.Activator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;


public class StopSSDHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(Activator.getProject()!=null){
			Activator.clearProject();
			Activator.setReadyForReconcile(false);
		}
		// TODO Auto-generated method stub
		return null;
	}

}
