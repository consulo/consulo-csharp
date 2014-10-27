package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.Collection;
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
	public CSharpElementGroup<CSharpArrayMethodDeclaration> indexMethodGroup()
	{
		List<CSharpElementGroup<CSharpArrayMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpArrayMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpArrayMethodDeclaration> elementGroup = context.indexMethodGroup();
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
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type)
	{
		List<CSharpElementGroup<CSharpMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpMethodDeclaration> elementGroup = context.findOperatorGroupByTokenType(type);
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl<CSharpMethodDeclaration>(myProject, groups);
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef)
	{
		List<CSharpElementGroup<CSharpConversionMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpConversionMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup<CSharpConversionMethodDeclaration> elementGroup = context.findConversionMethodGroup(typeRef);
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

	@NotNull
	@Override
	public Collection<CSharpElementGroup<CSharpMethodDeclaration>> getExtensionMethodGroups()
	{
		List<CSharpElementGroup<CSharpMethodDeclaration>> groups = new SmartList<CSharpElementGroup<CSharpMethodDeclaration>>();
		for(CSharpResolveContext context : myContexts)
		{
			groups.addAll(context.getExtensionMethodGroups());
		}
		return groups;
	}

	@NotNull
	@Override
	public PsiElement[] findByName(@NotNull String name, @NotNull UserDataHolder holder)
	{
		PsiElement[] array = PsiElement.EMPTY_ARRAY;
		for(CSharpResolveContext context : myContexts)
		{
			PsiElement[] byName = context.findByName(name, holder);
			array = ArrayUtil.mergeArrays(array, byName);
		}
		return array;
	}

	@NotNull
	@Override
	@SuppressWarnings("unchecked")
	public Collection<? extends PsiElement> getElements()
	{
		List groups = new SmartList();
		for(CSharpResolveContext context : myContexts)
		{
			groups.addAll(context.getElements());
		}
		return groups;
	}
}
