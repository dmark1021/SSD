package ssd.ASTVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
public class VarDecVisitor extends ASTVisitor {
private static ArrayList<String> validEncryptionMethods = new ArrayList<String>(Arrays.asList("encrypt"));
private CompilationUnit cu;
public VarDecVisitor(CompilationUnit cu) {
this.cu = cu;
}
private static <T> List<T> castList(Class<? extends T> cl, Collection<?> c) {
List<T> r = new ArrayList<T>(c.size());
for(Object o: c)
r.add(cl.cast(o));
return r;
}
private static boolean isValidEncryption(String methName) {
for(String s : validEncryptionMethods) {
if(s.equals(methName)) {
return true;
}
}
return false;
}
private static void createEncryptionProblemMarker(CompilationUnit cu, int lineNum) {
System.out.println("ERROR: sensitive data not encrypted!");
try {
IMarker marker = ((ICompilationUnit) (cu.getJavaElement())).getUnderlyingResource().createMarker(IMarker.PROBLEM);
marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
marker.setAttribute(IMarker.MESSAGE, "Sensitive data is not encrypted!");
marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
} catch (CoreException e1) {
e1.printStackTrace();
}
}
@Override
public boolean visit(VariableDeclarationStatement vd) {
List<IExtendedModifier> mods = castList(IExtendedModifier.class, vd.modifiers());
if(mods.size() < 0) { return false; } // If no modifiers, do nothing
/* If the data is annotated as sensitive */
String annName = ((SimpleName)((Annotation) mods.get(0)).getTypeName()).getIdentifier();
if(annName.equals("sensitive")) {
System.out.println("Found new Sensitive data: " + vd.toString());
/* If the data isn't initialized as call to a valid encryption method then this is an error */
List<VariableDeclarationFragment> frags = castList(VariableDeclarationFragment.class, vd.fragments());
Expression e = frags.get(0).getInitializer();
if(e.getClass() == MethodInvocation.class) {
String methName = ((SimpleName)((MethodInvocation) e).getName()).getIdentifier();
if(!isValidEncryption(methName)) {
createEncryptionProblemMarker(cu, cu.getLineNumber(e.getStartPosition()));
return false;
}
} else {
createEncryptionProblemMarker(cu, cu.getLineNumber(e.getStartPosition()));
return false;
}
}
return super.visit(vd);
}
}
