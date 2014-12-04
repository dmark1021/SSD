package ssd.analysis;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
public class MethodSearchEngine {
	private class MethodCollector extends SearchRequestor {
		private IMethod method;

		public MethodCollector() {
		}

		public IMethod getResult() {
			return method;
		}	

		/* (non-Javadoc)
		 * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org.eclipse.jdt.core.search.SearchMatch)
		 */
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			Object enclosingElement = match.getElement();
			if (enclosingElement instanceof IMethod) { // defensive code
				IMethod curr= (IMethod) enclosingElement;
					method=curr;
			}
		}
	}

	/**
	 * Searches for all main methods in the given scope.
	 * Valid styles are IJavaElementSearchConstants.CONSIDER_BINARIES and
	 * IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS
	 * 
	 * @param pm progress monitor
	 * @param scope search scope
	 * @param includeSubtypes whether to consider types that inherit a main method
	 */	
	public IMethod searchMethods(IJavaSearchScope scope, String strpattern) {
		SearchPattern pattern = SearchPattern.createPattern(strpattern, IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE); //$NON-NLS-1$
		SearchParticipant[] participants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
		MethodCollector collector = new MethodCollector();
		try {
			new SearchEngine().search(pattern, participants, scope, collector, null);
		} catch (CoreException ce) {
			ce.printStackTrace();
		}

		IMethod result = collector.getResult();

		return result;
	}
}
