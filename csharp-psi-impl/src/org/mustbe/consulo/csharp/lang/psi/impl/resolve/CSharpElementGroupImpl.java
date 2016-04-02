package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.Collection;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpOperatorNameHelper;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public class CSharpElementGroupImpl<T extends PsiElement> extends LightElement implements CSharpElementGroup<T>
{
	private final Object myKey;
	private final Collection<T> myElements;

	public CSharpElementGroupImpl(@NotNull Project project, @NotNull Object key, Collection<T> elements)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myKey = key;
		myElements = elements;
	}

	@Override
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
			@NotNull ResolveState state,
			PsiElement lastParent,
			@NotNull PsiElement place)
	{
		for(T element : myElements)
		{
			if(!processor.execute(element, state))
			{
				return false;
			}
		}
		return true;
	}

	@NotNull
	@Override
	public String getName()
	{
		if(myKey instanceof IElementType)
		{
			return CSharpOperatorNameHelper.getOperatorName((IElementType) myKey);
		}
		return myKey.toString();
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		for(T element : myElements)
		{
			if(element instanceof PsiNamedElement)
			{
				((PsiNamedElement) element).setName(name);
			}
		}
		return this;
	}

	@NotNull
	@Override
	public Collection<T> getElements()
	{
		return myElements;
	}

	@Override
	public boolean process(@NotNull Processor<? super T> processor)
	{
		for(T element : myElements)
		{
			if(!processor.process(element))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canNavigate()
	{
		return true;
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		for(PsiElement element : getElements())
		{
			if(element instanceof Navigatable)
			{
				((Navigatable) element).navigate(requestFocus);
				break;
			}
		}
	}

	@Override
	@NotNull
	public Object getKey()
	{
		return myKey;
	}

	@Override
	public String toString()
	{
		return "CSharpElementGroup: " + getName();
	}
}
