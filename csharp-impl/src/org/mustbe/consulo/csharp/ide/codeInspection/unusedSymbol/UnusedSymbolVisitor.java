package org.mustbe.consulo.csharp.ide.codeInspection.unusedSymbol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class UnusedSymbolVisitor extends CSharpElementVisitor
{
	private Map<PsiNameIdentifierOwner, Boolean> myVariableStates = new HashMap<PsiNameIdentifierOwner, Boolean>();

	@Override
	public void visitLocalVariable(CSharpLocalVariable variable)
	{
		Query<PsiReference> query = ReferencesSearch.search(variable, variable.getUseScope());

		myVariableStates.put(variable, query.findFirst() != null);
	}

	@NotNull
	public Collection<Map.Entry<PsiNameIdentifierOwner, Boolean>> getVariableStates()
	{
		return myVariableStates.entrySet();
	}
}
