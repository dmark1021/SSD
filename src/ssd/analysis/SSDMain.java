package ssd.analysis;

import ssd.Activator;
import ssd.ASTVisitor.ASTNodeAnnotationVisitor;
import ssd.ASTVisitor.MethodVisitor;
import ssd.ast.ASTBuilder;
import ssd.marker.SSDMarkerUtil;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
/**
 * This is the main class. All analysis will be done here
 * @author Obaida
 *
 */
public class SSDMain {
IJavaProject project;
IPackageFragment[] packages=null;
ArrayList<IPackageFragment> userpackages=new ArrayList<IPackageFragment>();
private Set<ASTNode> annotatedNodes=new HashSet<ASTNode>();
private Set<String> varKeys=new HashSet<String>();
private static IMethod doGetMethod;
private static IMethod doPostMethod;
public static BitSet cacheforGet=new BitSet(3); //All bits are initialized to zero by default
public static BitSet cacheforPost=new BitSet(3);//All bits are initialized to zero by default
public static ArrayList<IMarker> SSDMarkers=new ArrayList<IMarker>();

long startMem;
long startTime;

Map <ICompilationUnit,CompilationUnit> cunits=new HashMap<ICompilationUnit, CompilationUnit>();
public SSDMain(){
	this.project=Activator.getProject();
}

private void clearStaticVars() {
	annotatedNodes.clear();
	varKeys.clear();
	cacheforGet.clear();
	cacheforPost.clear();
	SSDMarkers.clear();
	
}
/**
 * This is the main method for SSD
 */
public void runStartingAnalysis() {
	//check if project is set
	if(project==null) {
		System.out.println("Java project is not set");
		return;
		
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
	getdoGetMethod();
	getdoPostMethod();
	runAnalysis();
	System.out.println("starting analysis complete");
}
public void runAnalysis() {
	System.out.println("............start analysis........");
	startingMemAndTime();
	//clear residual marker from previous run
	SSDMarkerUtil.clearStaleMarkers(SSDMarkers);
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
//clean static vars if there is any residue from previous run
clearStaticVars();
//will set annotatedNodes and varkeys
getSensitiveVars();
if(varKeys.isEmpty()){
	System.out.println("No sensitive data in the program, abort analysis");
	return;
}

//will visit doGet and all methods called by doGet
if(doGetMethod!=null){
	visitMehtods(doGetMethod);
	checkCacheSettings(doGetMethod);
}
//will visit doPost and all methods called by doPost
if(doPostMethod!=null){
	visitMehtods(doPostMethod);
	checkCacheSettings(doPostMethod);
}


System.out.println("list of sensitive variables"+varKeys.toString());
endingMemAndTime();
System.out.println("............complete........");
}

private void checkCacheSettings(IMethod method) {
	String msg;
	ASTNode methodNode;
	IMarker marker;
	if(method.equals(doGetMethod)){
		if(cacheforGet.cardinality()!=3){
			msg="Caching is not disabled properly in doGet method";
			System.out.println(msg);
			methodNode=getASTNode(method);
			marker=SSDMarkerUtil.addSSDMarker(null, msg, methodNode);
			SSDMarkers.add(marker);
			
		}
	}
	
	if(method.equals(doPostMethod)){
		if(cacheforPost.cardinality()!=3){
			msg="Caching is not disabled properly in doPost method";
			System.out.println(msg);
			methodNode=getASTNode(method);
			marker=SSDMarkerUtil.addSSDMarker(null, msg, methodNode);
			SSDMarkers.add(marker);
		}
	}
	
}

private void visitMehtods(IMethod parentMehtod){
	
	LinkedList<IMethod>methodsToVisit=new LinkedList<IMethod>();
	methodsToVisit.add(parentMehtod);
	
	IMethod method;
	while(!methodsToVisit.isEmpty()){
	method=methodsToVisit.remove();
//will send varaccess information too, because same var need to be checked for same thread.
	 MethodVisitor methodvisitor=new MethodVisitor(parentMehtod);
	 ASTNode methodnode=getASTNode(method);
		methodnode.accept(methodvisitor);
		Set<IMethod> calledmethods=methodvisitor.getCalledmethods();
		if(calledmethods!=null) {
		methodsToVisit.addAll(calledmethods);
		}
	}
	
}

private ASTNode getASTNode(IMethod method){
	String key = method.getKey();
//	 create AST using astbuilder then accept the visitor for checking run method
	CompilationUnit methodCU=ASTBuilder.getASTBuilder().parse(method.getCompilationUnit());
	 ASTNode methodnode=methodCU.findDeclaringNode(key);
	 return methodnode;
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
	Set<ASTNode> annnode;
	for(Map.Entry<ICompilationUnit,CompilationUnit> entry:cunits.entrySet()){
		CompilationUnit cu=entry.getValue();
		ICompilationUnit icu=entry.getKey();
		ASTNodeAnnotationVisitor annvisitor=new ASTNodeAnnotationVisitor(annotatedNodes,varKeys);
		cu.accept(annvisitor);
		annnode=annvisitor.getAnnotatedNodes();
		if(!annnode.isEmpty()){
		annotatedNodes.addAll(annnode);
		varKeys.addAll(annvisitor.getVarKeys());
		}
	}
	
}

private void getdoGetMethod(){
	String pattern="doGet(HttpServletRequest, HttpServletResponse) void";
	IPackageFragment[] userpackagearray=new IPackageFragment[userpackages.size()];
	userpackages.toArray(userpackagearray);
	IJavaSearchScope scope=SearchEngine.createJavaSearchScope(userpackagearray, false);
	MethodSearchEngine methodsearch=new MethodSearchEngine();
	SSDMain.doGetMethod=methodsearch.searchMethods(scope,pattern);
	System.out.println(doGetMethod);
	
}

private void getdoPostMethod(){
	String pattern="doPost(HttpServletRequest, HttpServletResponse) void";
	IPackageFragment[] userpackagearray=new IPackageFragment[userpackages.size()];
	userpackages.toArray(userpackagearray);
	IJavaSearchScope scope=SearchEngine.createJavaSearchScope(userpackagearray, false);
	MethodSearchEngine methodsearch=new MethodSearchEngine();
	SSDMain.doPostMethod=methodsearch.searchMethods(scope,pattern);
	System.out.println(doPostMethod);
	
}

//Will get all the ASTs and put it in cunits
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
