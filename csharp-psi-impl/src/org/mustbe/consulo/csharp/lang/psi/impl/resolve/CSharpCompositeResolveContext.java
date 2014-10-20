package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
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
	public CSharpElementGroup indexMethodGroup()
	{
		List<CSharpElementGroup> groups = new SmartList<CSharpElementGroup>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup elementGroup = context.indexMethodGroup();
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl(myProject, groups);
	}

	@Nullable
	@Override
	public CSharpElementGroup constructorGroup()
	{
		List<CSharpElementGroup> groups = new SmartList<CSharpElementGroup>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup elementGroup = context.constructorGroup();
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl(myProject, groups);
	}

	@Nullable
	@Override
	public CSharpElementGroup deConstructorGroup()
	{
		List<CSharpElementGroup> groups = new SmartList<CSharpElementGroup>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup elementGroup = context.deConstructorGroup();
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl(myProject, groups);
	}

	@Nullable
	@Override
	public CSharpElementGroup findOperatorGroupByTokenType(@NotNull IElementType type)
	{
		List<CSharpElementGroup> groups = new SmartList<CSharpElementGroup>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup elementGroup = context.findOperatorGroupByTokenType(type);
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl(myProject, groups);
	}

	@Nullable
	@Override
	public CSharpElementGroup findExtensionMethodByName(@NotNull String name)
	{
		List<CSharpElementGroup> groups = new SmartList<CSharpElementGroup>();
		for(CSharpResolveContext context : myContexts)
		{
			CSharpElementGroup elementGroup = context.findExtensionMethodByName(name);
			if(elementGroup != null)
			{
				groups.add(elementGroup);
			}
		}
		return groups.isEmpty() ? null : new CSharpCompositeElementGroupImpl(myProject, groups);
	}

	@Nullable
	@Override
	public PsiElement findByName(@NotNull String name, @NotNull UserDataHolder holder)
	{
		for(CSharpResolveContext context : myContexts)
		{
			PsiElement byName = context.findByName(name, holder);
			if(byName != null)
			{
				return byName;
			}
		}
		return null;
	}
}
