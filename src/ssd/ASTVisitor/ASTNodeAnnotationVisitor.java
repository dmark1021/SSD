package ssd.ASTVisitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;


//import edu.uncc.sis.aside.utils.ASIDEMarkerAndAnnotationUtil;

public class ASTNodeAnnotationVisitor extends ASTVisitor {
	
	private ArrayList<ASTNode> annotatedNodes;
	private ArrayList<String> varKeys;
	


	public void setVarKeys(ArrayList<String> varKeys) {
		this.varKeys = varKeys;
	}

	public ASTNodeAnnotationVisitor(){
		annotatedNodes = new ArrayList<ASTNode>();
		varKeys=new ArrayList<String>();
	}

    public ArrayList<ASTNode> getAnnotatedNodes(){
    	return annotatedNodes;
    }	
    
	public ArrayList<String> getVarKeys() {
		return varKeys;
	}
    

@Override
	public boolean visit(VariableDeclarationFragment node) {
	String varKey;
	IBinding ib=node.getName().resolveBinding();
	IAnnotationBinding[] iab=ib.getAnnotations();
	if(iab.length!=0){
		for (int i = 0; i < iab.length; i++) {
			if(iab[i].getName().equals("sensitive")){
				
				IVariableBinding ivb=(IVariableBinding) ib;
				IVariableBinding vbDec=ivb.getVariableDeclaration();
				varKey=ivb.getKey();
				annotatedNodes.add(node);
				varKeys.add(varKey);
			}
		}
	}
		// TODO Auto-generated method stub
		return super.visit(node);
	}


}
