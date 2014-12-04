package compilation;

import ssd.Activator;

import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ssd.analysis.SSDMain;

public class SSDcompilationParticipant extends CompilationParticipant {

	public SSDcompilationParticipant() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int aboutToBuild(IJavaProject project) {

		return READY_FOR_BUILD;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.CompilationParticipant#isActive(org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public boolean isActive(IJavaProject project) {
		// TODO Auto-generated method stub
//		return super.isActive(project);
		return true;
	}

	@Override
	public void buildFinished(IJavaProject project) {
		// do nothing by default
		System.out.println("build finished");
	}
	public void reconcile(ReconcileContext context) {
		if(!Activator.isReadyForReconcile()) {
			System.out.println("Start SSD");
			return;}
		IJavaElementDelta delta = context.getDelta();
		if (delta == null) {
			System.out
					.println("javaElementDelta == null in SSDCompilationParticipant");
			return;
		}
		IJavaProject project = delta.getElement()
				.getJavaProject();

		if (project == null) {
			System.out
					.println("project == null in AsideCompilationParticipant");
			return;
		}
		int kind = delta.getKind();

		/* consider changes on one java file only */
		if (kind != IJavaElementDelta.CHANGED) {
			// System.out.println("kind != IJavaElementDelta.CHANGED in AsideCompilationParticipant");
			return;
		}

		int flags = delta.getFlags();
		if ((flags & IJavaElementDelta.F_CONTENT) != 0
				|| (flags & IJavaElementDelta.F_AST_AFFECTED) != 0) {

			CompilationUnit compilationUnitASTAfterReconcile = null;
			try {
				compilationUnitASTAfterReconcile = context
						.getAST8();
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (compilationUnitASTAfterReconcile == null) {
				return;
			}

		
		SSDMain ssdmain=new SSDMain();
		ssdmain.runAnalysis();
		System.out.println("reconcile");
			
}
	}
}
