package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpCompositeResolveContext implements CSharpResolveContext
{
	private final Project myProject;
	private final CSharpResolveContext[] myContexts;

	public CSharpCompositeResolveContext(Project project, CSharpResolveContext... contexts)
	{
		myProject = project;
		myContexts = contexts;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpArrayMethodDeclaration> indexMethodGroup(boolean deep)
	{
		List<CSharpElementGroup<CSharpArrayMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpArrayMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpArrayMethodDeclaration> elementGroup = context.indexMethodGroup(deep);
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpArrayMethodDeclaration>(myProject, groups);
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup()
	{
		List<CSharpElementGroup<CSharpConstructorDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpConstructorDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpConstructorDeclaration> elementGroup = context.constructorGroup();
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpConstructorDeclaration>(myProject, groups);
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup()
	{
		List<CSharpElementGroup<CSharpConstructorDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpConstructorDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpConstructorDeclaration> elementGroup = context.deConstructorGroup();
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpConstructorDeclaration>(myProject, groups);
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type, boolean deep)
	{
		List<CSharpElementGroup<CSharpMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpMethodDeclaration> elementGroup = context.findOperatorGroupByTokenType(type, deep);
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpMethodDeclaration>(myProject, groups);
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef, boolean deep)
	{
		List<CSharpElementGroup<CSharpConversionMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpConversionMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpConversionMethodDeclaration> elementGroup = context.findConversionMethodGroup(typeRef, deep);
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpConversionMethodDeclaration>(myProject, groups);
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
	{
		List<CSharpElementGroup<CSharpMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpMethodDeclaration> elementGroup = context.findExtensionMethodGroupByName(name);
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpMethodDeclaration>(myProject, groups);
	}

	@Override
	public boolean processExtensionMethodGroups(@NotNull Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		for(CSharpResolveContext context : myContexts)
		{
			if(!context.processExtensionMethodGroups(processor))
			{
				return false;
			}
		}
		return true;
	}

	@NotNull
	@Override
	public PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder)
	{
		PsiElement[] array = PsiElement.EMPTY_ARRAY;
		for(CSharpResolveContext context : myContexts)
		{
			PsiElement[] byName = context.findByName(name, deep, holder);
			array = ArrayUtil.mergeArrays(array, byName);
		}
		return array;
	}

	@Override
	public boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep)
	{
		for(CSharpResolveContext context : myContexts)
		{
			if(!context.processElements(processor, deep))
			{
				return false;
			}
		}
		return true;
	}
}
