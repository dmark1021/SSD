package ssd.analysis;

import ssd.Activator;
import ssd.ASTVisitor.ASTNodeAnnotationVisitor;
import ssd.ASTVisitor.runMethodVisitor;
import ssd.ast.ASTBuilder;
import ssd.ast.VarDecVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
public class SSDMain {
IJavaProject project;
IPackageFragment[] packages=null;
ArrayList<IPackageFragment> userpackages=new ArrayList<IPackageFragment>();
private ArrayList<ASTNode> annotatedNodes=new ArrayList<ASTNode>();
private ArrayList<String> varKeys=new ArrayList<String>();
private IMethod doGetMethod;
private IMethod doPostMethod;

long startMem;
long startTime;

Map <ICompilationUnit,CompilationUnit> cunits=new HashMap<ICompilationUnit, CompilationUnit>();
public SSDMain(){
	this.project=Activator.getProject();
}

private void initializeStaticVars() {
	annotatedNodes.clear();
	varKeys.clear();
	
}
/**
 * This is the main method for SSD
 */
public void runStartingAnalysis() {
	//check if project is set
	if(project==null) {
		System.out.println("Java project is not set");
		
	}
	else {
		try {
			//set the array userpackages, required for searching main method
			setUserpackage();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//will set the main IMethod
	}
	runAnalysis();
	System.out.println("starting analysis complete");
}
public void runAnalysis() {
	startingMemAndTime();
	//check if project is set
	if(project==null) {
		System.out.println("Java project is not set");
		return;
		
	}

try {
	/**
	 * will get compilation units for all source code
	 * will set cunits
	 */
	getAST();
} catch (JavaModelException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

if(cunits.isEmpty()){
	System.out.println("No compilation unit found. Abort analyis");
	return;
}
//will set annotatedNodes and varkeys

getSensitiveVars();
getdoGetMethod();

	System.out.println("complete");
	endingMemAndTime();
}




public void setUserpackage() throws JavaModelException{
	 this.packages=project.getPackageFragments();

	 for (IPackageFragment mypackage : packages) {
//	       We will only look at the package from the source folder
	      if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
	    	  userpackages.add(mypackage);
	       
	      }
	    }
}



private void getSensitiveVars(){
	ArrayList<ASTNode> annnode;
	for(Map.Entry<ICompilationUnit,CompilationUnit> entry:cunits.entrySet()){
		CompilationUnit cu=entry.getValue();
		ICompilationUnit icu=entry.getKey();
		ASTNodeAnnotationVisitor annvisitor=new ASTNodeAnnotationVisitor();
		cu.accept(annvisitor);
		annnode=annvisitor.getAnnotatedNodes();
		if(!annnode.isEmpty()){
		annotatedNodes.addAll(annnode);
		varKeys.addAll(annvisitor.getVarKeys());
		}
	}
	
}

public void getdoGetMethod(){
	String pattern="doGet(HttpServletRequest, HttpServletResponse) void";
	IPackageFragment[] userpackagearray=new IPackageFragment[userpackages.size()];
	userpackages.toArray(userpackagearray);
	IJavaSearchScope scope=SearchEngine.createJavaSearchScope(userpackagearray, false);
	MethodSearchEngine methodsearch=new MethodSearchEngine();
	this.doGetMethod=methodsearch.searchMethods(scope,pattern);
	System.out.println(doGetMethod);
	
}






//currently not using this method. In future may use again
public void getAST() throws JavaModelException{
	 this.packages=project.getPackageFragments();

	 for (IPackageFragment mypackage : packages) {
//	       We will only look at the package from the source folder
	      if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
	    	  userpackages.add(mypackage);
	        System.out.println("Package " + mypackage.getElementName());
	        for (ICompilationUnit unit : mypackage.getCompilationUnits()) { //IcompilationUnit is any source file
	        	cunits.put(unit,ASTBuilder.getASTBuilder().parse(unit)); //store in the map for later use
	          }
	      }
	    }
}
/**
 * starting time and memory  is recorded
 */
private void startingMemAndTime() {
	System.gc();
	System.gc();
	System.gc();
	System.gc();
	System.gc();
	this.startMem = Runtime.getRuntime().totalMemory()
			- Runtime.getRuntime().freeMemory();
	this.startTime = System.currentTimeMillis();
	
}
/**
 * @param null
 * Total runtime and memory used is calculated
 * @return null
 */
private void endingMemAndTime() {
	// mem and time measurements
	System.out.println("Analysis time="+(System.currentTimeMillis() - startTime)
			/ 1000.0 + " seconds");
	System.gc();
	System.gc();
	System.gc();
	System.gc();
	System.gc();
	System.out.println("Memory used="+(Runtime.getRuntime().totalMemory()
			- Runtime.getRuntime().freeMemory() - startMem)
			/ (1024 * 1024.0) + " MB");
	
}

}
