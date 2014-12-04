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
public class runMethodVisitor extends ASTVisitor {
Map<String, Integer> varAccess=new HashMap<String, Integer>();
Map<String, ASTNode> varNode=new HashMap<String, ASTNode>();
Set<IMethod> calledmethods= new HashSet<IMethod>();


IMethod method;
IResource resource;
int read=0;
int write=1;
String threadname;

	public runMethodVisitor(String threadname,IMethod method,Map<String, Integer> varAccess) {
	this.threadname =threadname;
	this.method=method;
	if(varAccess!=null) {
	this.varAccess=varAccess;
	}
	this.resource=method.getResource();
}
	@Override
	public boolean visit(Assignment node) {
		Expression leftside=node.getLeftHandSide();
		resolveExpression(leftside, write, node);
		Expression rightside=node.getRightHandSide();
		resolveExpression(rightside, read,node);
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
			resolveExpression(initializer, read, node);
		}
		return super.visit(node);
	}
	@Override
	public boolean visit(PostfixExpression node) {
		Expression operand=node.getOperand();
		resolveExpression(operand, write, node);
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	@Override
	public boolean visit(PrefixExpression node) {
		Expression operand=node.getOperand();
		resolveExpression(operand, write,node);
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
					//arguments can be infixexpression, handled in resolveExpression method
					resolveExpression(it.next(), read, node);
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
	resolveExpression(node, read, node);
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
				resolveExpression(it.next(), read, node);
			}
		}
		
	}
	// TODO Auto-generated method stub
	return super.visit(node);
}


	public Map<String, Integer> getVarAcess() {
		return varAccess;
	}
	
	public Map<String, ASTNode> getVarNode() {
		return varNode;
	}
	/**
	 * @return the calledmethods
	 */
	public Set<IMethod> getCalledmethods() {
		return calledmethods;
	}
	
	private void resolveExpression(Expression exp, int accesstype, ASTNode node) {
		if(exp instanceof org.eclipse.jdt.core.dom.NumberLiteral || exp instanceof StringLiteral) {
			return;
		}
		/**
		 * check if inside a synchronized statement
		 */
		
		@SuppressWarnings("restriction")
		ASTNode synchronizedparent=ASTResolving.findAncestor(node, ASTNode.SYNCHRONIZED_STATEMENT);
		if(synchronizedparent!=null) {
			return;
		}
		
		/**
		 * check if any expression is infix expression. Do recursive call
		 * this is useful for arguments of library calls
		 * any other infixexpression is not handled separately, directly send to here.
		 */
		if(exp instanceof InfixExpression) {
			InfixExpression iexp=(InfixExpression) exp;
			Expression leftoperand=iexp.getLeftOperand();
			Expression rightoperand=iexp.getRightOperand();
			resolveExpression(leftoperand, accesstype, node);
			resolveExpression(rightoperand, accesstype, node);
			if(iexp.hasExtendedOperands()) {
				List<Expression> list=iexp.extendedOperands();
				Iterator<Expression> it=list.iterator();
				while(it.hasNext()) {
					resolveExpression(it.next(), accesstype, node);
					
				}
			}
			
		}
		//handle array access
		if(exp instanceof ArrayAccess) {
			exp=((ArrayAccess) exp).getArray();
		}
		//QualifiedName is used to find variable binding and variable declaration
		if(exp instanceof QualifiedName || exp instanceof SimpleName) {
		IBinding ib=((Name) exp).resolveBinding();
		if(ib==null) {
			System.out.println("cannto resolve binding for"+exp.toString());
			return;
		}
		int modifier=ib.getModifiers();
		//check if the field has static modifier, check if the binding is variable
		if(((modifier & Modifier.STATIC)==Modifier.STATIC) && (ib.getKind()==IBinding.VARIABLE)){
			IVariableBinding vb=(IVariableBinding)ib;
			IVariableBinding vbDec=vb.getVariableDeclaration();
//			String classname=vb.getDeclaringClass().getQualifiedName();
//			String varname=vb.getName();
//			varname=classname+"."+varname;
			//key is unique for each variable in the project
			String varname=vbDec.getKey();
			int pos=exp.getStartPosition();
			if(!varAccess.isEmpty() && varAccess.containsKey(varname) && varAccess.get(varname)==write) {return;}
			else {
				varAccess.put(varname, accesstype);
				varNode.put(varname, node);
				
			}
		}}
	}
	

	
}
