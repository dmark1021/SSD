package ssd.ASTVisitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.QualifiedName;

public class expressionVisitor extends ASTVisitor {
	int accesstype;
	String threadname;
	

	public expressionVisitor(int accesstype, String threadname) {
		this.accesstype = accesstype;
		this.threadname = threadname;
	}


	@Override
	public boolean visit(QualifiedName node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	

}
