package ssd.ASTVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ssd.analysis.SSDMain;
import ssd.marker.SSDMarkerUtil;



public class ASTNodeAnnotationVisitor extends ASTVisitor {
	
	private Set<ASTNode> annotatedNodes=new HashSet<ASTNode>();
	private Set<String> varKeys=new HashSet<String>();
	private Map<String,MethodInvocation> LeakingSensitiveVars=new HashMap<String,MethodInvocation>();
	

	public Map<String, MethodInvocation> getLeakingSensitiveVars() {
		return LeakingSensitiveVars;
	}

	public ASTNodeAnnotationVisitor(Set<ASTNode> annotatedNodes,Set<String> varKeys){
		this.annotatedNodes =annotatedNodes; 
		this.varKeys=varKeys;
	}

    public Set<ASTNode> getAnnotatedNodes(){
    	return annotatedNodes;
    }	
    
	public Set<String> getVarKeys() {
		return varKeys;
	}
    

@Override
	public boolean visit(VariableDeclarationFragment node) {
	String varKey=null;
	boolean hasSensitiveAnnotation=false;
	IBinding ib=node.getName().resolveBinding();
	IAnnotationBinding[] iab=ib.getAnnotations();
	if(iab.length!=0){
		for (int i = 0; i < iab.length; i++) {
			if(iab[i].getName().equals("sensitive")){
				
				IVariableBinding ivb=(IVariableBinding) ib;
				IVariableBinding vbDec=ivb.getVariableDeclaration();
				varKey=vbDec.getKey();
				annotatedNodes.add(node);
				varKeys.add(varKey);
				hasSensitiveAnnotation=true;
			}
			//check if initializer is sensitive

		}
	}
	if(hasSensitiveAnnotation==false){
		Expression exp=node.getInitializer();
		if(isSensitive(exp)==true){
			IVariableBinding ivb=(IVariableBinding) ib;
			IVariableBinding vbDec=ivb.getVariableDeclaration();
			varKey=vbDec.getKey();
			annotatedNodes.add(node);
			varKeys.add(varKey);
		}
	}
		// TODO Auto-generated method stub
		return super.visit(node);
	}
@Override
public boolean visit(Assignment node) {
	Expression leftside=node.getLeftHandSide();
	Expression rightside=node.getRightHandSide();
	if(isSensitive(rightside)==true){
		String varkey=getVarKeyFromExpression(leftside);
		if(varkey!=null){
		annotatedNodes.add(node);
		varKeys.add(varkey);
		}
		
		if(!(rightside instanceof InfixExpression)){
			//unnecessary copying of sensitive variables
			String msg="unnecessary copying of sensitive varialbe.\""+rightside.toString()+"\" is sensitive data";
			IMarker marker=SSDMarkerUtil.addSSDMarker(msg, node);
			SSDMain.SSDMarkers.add(marker);
			
		}
	}
	// TODO Auto-generated method stub
	return super.visit(node);
}



@Override
public boolean visit(MethodInvocation node) {
	getSensitiveDataWrite(node);
	return super.visit(node);
}

private void getSensitiveDataWrite(MethodInvocation node) {
	Set<String> sensitiveVars;
	IMethodBinding mb=node.resolveMethodBinding();
	ITypeBinding tb=mb.getDeclaringClass();
	String classname=tb.getQualifiedName();
	if(classname.equals("java.io.PrintWriter")){
		List<Expression> arguments=node.arguments();
		if(arguments!=null) {
			Iterator<Expression> it=arguments.iterator();
			while(it.hasNext()) {
				Expression arg=it.next();
				sensitiveVars=getSensitiveVarList(arg);
				if(sensitiveVars!=null){
				for(String varname:sensitiveVars){
					this.LeakingSensitiveVars.put(varname, node);
				}
				}
				

			}
		}
	}
}

/**
 * 
 * @param exp
 * @return set of sensitive variables if has any, else null
 */
private Set<String> getSensitiveVarList(Expression exp){
	Set<String> sensitiveVars=new HashSet<String>();
	sensitiveVars.clear();
	boolean sensitive=false;
	if(exp instanceof org.eclipse.jdt.core.dom.NumberLiteral || exp instanceof StringLiteral) {
		return null;
	}
	/**
	 * check if any expression is infix expression. Do recursive call
	 * this is useful for arguments of library calls
	 * any other infixexpression is not handled separately, directly send to here.
	 */
	if(exp instanceof InfixExpression) {
		InfixExpression iexp=(InfixExpression) exp;
		Expression leftoperand=iexp.getLeftOperand();
		sensitive=isSensitive(leftoperand);
		if(sensitive==true){
			sensitiveVars.add(getVarKeyFromExpression(leftoperand));
		}
		Expression rightoperand=iexp.getRightOperand();
		sensitive=isSensitive(rightoperand);
		if(sensitive==true){
			sensitiveVars.add(getVarKeyFromExpression(rightoperand));
		}
		if(iexp.hasExtendedOperands()) {
			List<Expression> list=iexp.extendedOperands();
			Iterator<Expression> it=list.iterator();
			while(it.hasNext()) {
				sensitive=isSensitive(it.next());
				if(sensitive==true){
					sensitiveVars.add(getVarKeyFromExpression(it.next()));
				}
				
			}
		}
		
	}


		String varname=getVarKeyFromExpression(exp);
		if(varname!=null && !varKeys.isEmpty() && varKeys.contains(varname)) {
			sensitiveVars.add(varname);
			}

	if(!sensitiveVars.isEmpty()){
		return sensitiveVars;
	}
	return null;
	
	
}
private boolean isSensitive(Expression exp){
	boolean sensitive=false;
	if(exp instanceof org.eclipse.jdt.core.dom.NumberLiteral || exp instanceof StringLiteral) {
		return false;
	}
	/**
	 * check if any expression is infix expression. Do recursive call
	 * this is useful for arguments of library calls
	 * any other infixexpression is not handled separately, directly send to here.
	 */
	if(exp instanceof InfixExpression) {
		InfixExpression iexp=(InfixExpression) exp;
		Expression leftoperand=iexp.getLeftOperand();
		sensitive=isSensitive(leftoperand);
		if(sensitive==true){
			return sensitive;
		}
		Expression rightoperand=iexp.getRightOperand();
		sensitive=isSensitive(rightoperand);
		if(sensitive==true){
			return sensitive;
		}
		if(iexp.hasExtendedOperands()) {
			List<Expression> list=iexp.extendedOperands();
			Iterator<Expression> it=list.iterator();
			while(it.hasNext()) {
				sensitive=isSensitive(it.next());
				if(sensitive==true){
					return sensitive;
				}
				
			}
		}
		
	}


		String varname=getVarKeyFromExpression(exp);
		if(varname!=null && !varKeys.isEmpty() && varKeys.contains(varname)) {
			return true;
			}

	
	return sensitive;
	
}
private String getVarKeyFromExpression(Expression exp){
	//QualifiedName is used to find variable binding and variable declaration
	if(exp instanceof QualifiedName || exp instanceof SimpleName) {
	IBinding ib=((Name) exp).resolveBinding();
	if(ib==null) {
		System.out.println("cannto resolve binding for"+exp.toString());
		return null;
	}
	//check if the binding is variable
	if(ib.getKind()==IBinding.VARIABLE){
		IVariableBinding vb=(IVariableBinding)ib;
		IVariableBinding vbDec=vb.getVariableDeclaration();
		String varname=vbDec.getKey();
		return varname;

	}
	}
	return null;
	
}


}
