package ssd.ASTVisitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.compiler.ast.NumberLiteral;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
/**
 * Will visit every run method and every other method called inside
 * run method of a thread
 * list all assignment and classify the access type
 * extract the variable from both left hand and right hand side
 * store the result in static map created in idrcmain
 * aliasing problem is solved by using getVariableDeclaration() for every variable
 * getVariableDeclaration() always points to the declaration of the variable.
 * @author Obaida
 *
 */
public class MethodVisitor extends ASTVisitor {
Set<IMethod> calledmethods= new HashSet<IMethod>();
	public MethodVisitor() {
}
	@Override
	public boolean visit(Assignment node) {
		Expression leftside=node.getLeftHandSide();
		Expression rightside=node.getRightHandSide();
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationFragment)
	 */
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		// TODO Auto-generated method stub
		Expression initializer=node.getInitializer();
		if(initializer!=null) {
		}
		return super.visit(node);
	}
	@Override
	public boolean visit(PostfixExpression node) {
		Expression operand=node.getOperand();
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	@Override
	public boolean visit(PrefixExpression node) {
		Expression operand=node.getOperand();
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
/**
 * will visit every method call and add up to calledmethods for future visit.
 * Cannot handle constructor
 */
	@Override
	public boolean visit(MethodInvocation node) {
		System.out.println("invoke"+node.getName());
		IMethodBinding mb=node.resolveMethodBinding();
		IMethodBinding dmb=mb.getMethodDeclaration();
		IJavaElement je=dmb.getJavaElement();
		boolean readonly=je.isReadOnly();
		if(je!=null && !readonly) {
		IMethod im=(IMethod) je;
		calledmethods.add(im);
		}
		else {
			List<Expression> arguments=node.arguments();
			if(arguments!=null) {
				Iterator<Expression> it=arguments.iterator();
				while(it.hasNext()) {
					//option for handling library calls
				}
			}
			
		}
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	
	
	/* (non-Javadoc)
 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.InfixExpression)
 */
@Override
public boolean visit(InfixExpression node) {
	// TODO Auto-generated method stub
	return super.visit(node);
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ClassInstanceCreation)
 */
@Override
public boolean visit(ClassInstanceCreation node) {
	System.out.println("invoke"+node.toString());
	IMethodBinding mb=node.resolveConstructorBinding();
	IMethodBinding dmb=mb.getMethodDeclaration();
	IJavaElement je=dmb.getJavaElement();
	
	
	if(je!=null) {
		boolean readonly=je.isReadOnly();
		if(!readonly) {
		IMethod im=(IMethod) je;
		calledmethods.add(im);
	}}
	else {
		List<Expression> arguments=node.arguments();
		if(arguments!=null) {
			Iterator<Expression> it=arguments.iterator();
			while(it.hasNext()) {
				//option for handling library calls
			}
		}
		
	}
	// TODO Auto-generated method stub
	return super.visit(node);
}

	/**
	 * @return the calledmethods
	 */
	public Set<IMethod> getCalledmethods() {
		return calledmethods;
	}
	
	

	
}
