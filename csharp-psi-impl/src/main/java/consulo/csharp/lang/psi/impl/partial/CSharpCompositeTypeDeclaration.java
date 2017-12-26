/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.lang.psi.impl.partial;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ObjectUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import consulo.annotations.Immutable;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.resolve.CSharpPsiSearcher;
import consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 01.05.2015
 */
public class CSharpCompositeTypeDeclaration extends LightElement implements CSharpTypeDeclaration
{
	@RequiredReadAction
	@NotNull
	public static DotNetTypeDeclaration selectCompositeOrSelfType(@NotNull DotNetTypeDeclaration parent)
	{
		if(parent.hasModifier(CSharpModifier.PARTIAL))
		{
			CSharpCompositeTypeDeclaration compositeType = findCompositeType((CSharpTypeDeclaration) parent);
			return ObjectUtil.notNull(compositeType, parent);
		}
		return parent;
	}

	@RequiredReadAction
	@NotNull
	public static LocalSearchScope createLocalScope(@NotNull DotNetTypeDeclaration parent)
	{
		DotNetTypeDeclaration type = selectCompositeOrSelfType(parent);
		if(type instanceof CSharpCompositeTypeDeclaration)
		{
			return new LocalSearchScope(((CSharpCompositeTypeDeclaration) type).getTypeDeclarations());
		}
		else
		{
			return new LocalSearchScope(parent);
		}
	}

	@RequiredReadAction
	@Nullable
	public static CSharpCompositeTypeDeclaration findCompositeType(@NotNull CSharpTypeDeclaration parent)
	{
		Object cachedValue = CachedValuesManager.getCachedValue(parent, () -> CachedValueProvider.Result.create(findCompositeTypeImpl(parent), PsiModificationTracker
				.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT));
		return cachedValue == ObjectUtil.NULL ? null : (CSharpCompositeTypeDeclaration) cachedValue;
	}

	@RequiredReadAction
	private static Object findCompositeTypeImpl(@Deprecated CSharpTypeDeclaration typeDeclaration)
	{
		String vmQName = typeDeclaration.getVmQName();
		assert vmQName != null;
		DotNetTypeDeclaration[] types = CSharpPsiSearcher.getInstance(typeDeclaration.getProject()).findTypes(vmQName, typeDeclaration.getResolveScope());

		for(DotNetTypeDeclaration type : types)
		{
			if(type instanceof CSharpCompositeTypeDeclaration)
			{
				CSharpTypeDeclaration[] typeDeclarations = ((CSharpCompositeTypeDeclaration) type).getTypeDeclarations();
				if(ArrayUtil.contains(typeDeclaration, typeDeclarations))
				{
					return type;
				}
			}
		}

		return ObjectUtil.NULL;
	}

	@NotNull
	@RequiredReadAction
	public static Collection<PsiElement> wrapPartialTypes(@NotNull GlobalSearchScope scope, @NotNull Project project, @NotNull Collection<PsiElement> elements)
	{
		MultiMap<String, CSharpTypeDeclaration> partialTypes = null;

		List<PsiElement> newElementList = null;

		PsiElement[] psiElements = ContainerUtil.toArray(elements, PsiElement.ARRAY_FACTORY);

		for(int i = 0; i < psiElements.length; i++)
		{
			ProgressManager.checkCanceled();

			PsiElement psiElement = psiElements[i];
			if(psiElement instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) psiElement).hasModifier(CSharpModifier.PARTIAL))
			{
				String vmQName = ((CSharpTypeDeclaration) psiElement).getVmQName();
				if(vmQName != null)
				{
					if(partialTypes == null)
					{
						partialTypes = MultiMap.create();
					}

					if(newElementList == null)
					{
						newElementList = new ArrayList<>(psiElements.length);
						// we need copy head to new list
						newElementList.addAll(Arrays.asList(psiElements).subList(0, i));
					}

					partialTypes.putValue(vmQName, (CSharpTypeDeclaration) psiElement);
					continue;
				}
			}

			if(newElementList != null)
			{
				newElementList.add(psiElement);
			}
		}

		if(partialTypes == null)
		{
			return elements;
		}

		for(Map.Entry<String, Collection<CSharpTypeDeclaration>> entry : partialTypes.entrySet())
		{
			ProgressManager.checkCanceled();

			Collection<CSharpTypeDeclaration> value = entry.getValue();
			// partial modifier is useless, only one class with name
			if(value.size() == 1)
			{
				newElementList.add(value.iterator().next());
			}
			else
			{
				CSharpTypeDeclaration compositeType = CSharpPartialElementManager.getInstance(project).getOrCreateCompositeType(scope, entry.getKey(), value);

				newElementList.add(compositeType);
			}
		}
		return newElementList;
	}

	private Project myProject;
	private GlobalSearchScope mySearchScope;
	private CSharpTypeDeclaration[] myTypeDeclarations;

	public CSharpCompositeTypeDeclaration(@NotNull Project project, GlobalSearchScope searchScope, CSharpTypeDeclaration[] typeDeclarations)
	{
		super(typeDeclarations[0].getManager(), typeDeclarations[0].getLanguage());
		myProject = project;
		mySearchScope = searchScope;
		myTypeDeclarations = typeDeclarations;
	}

	@Override
	public <T> T getUserData(@NotNull Key<T> key)
	{
		if(key == ModuleUtilCore.KEY_MODULE)
		{
			//noinspection unchecked
			return (T) ModuleUtilCore.findModuleForPsiElement(myTypeDeclarations[0]);
		}
		return super.getUserData(key);
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return null;
	}

	@NotNull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return new CSharpGenericConstraint[0];
	}

	@Override
	public boolean canNavigate()
	{
		return true;
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		((Navigatable) myTypeDeclarations[0]).navigate(requestFocus);

	}

	@Override
	public boolean isInterface()
	{
		return false;
	}

	@Override
	public boolean isStruct()
	{
		return false;
	}

	@Override
	public boolean isEnum()
	{
		return false;
	}

	@Override
	public boolean isNested()
	{
		return false;
	}

	@Nullable
	@Override
	public DotNetTypeList getExtendList()
	{
		return null;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		List<DotNetTypeRef> extendTypeRefs = new SmartList<>();
		for(DotNetTypeDeclaration type : myTypeDeclarations)
		{
			DotNetTypeList extendList = type.getExtendList();
			if(extendList != null)
			{
				DotNetTypeRef[] typeRefs = extendList.getTypeRefs();
				Collections.addAll(extendTypeRefs, typeRefs);
			}
		}

		if(extendTypeRefs.isEmpty())
		{
			Set<String> set = new THashSet<>();
			for(DotNetTypeDeclaration type : myTypeDeclarations)
			{
				ContainerUtil.addIfNotNull(set, CSharpTypeDeclarationImplUtil.getDefaultSuperType(type));
			}

			if(set.contains(DotNetTypes.System.ValueType))
			{
				extendTypeRefs.add(new CSharpTypeRefByQName(myProject, mySearchScope, DotNetTypes.System.ValueType));
			}
			else
			{
				extendTypeRefs.add(new CSharpTypeRefByQName(myProject, mySearchScope, DotNetTypes.System.Object));
			}
		}
		return ContainerUtil.toArray(extendTypeRefs, DotNetTypeRef.ARRAY_FACTORY);
	}

	@RequiredReadAction
	@Override
	public boolean isInheritor(@NotNull String typeDeclaration, boolean b)
	{
		for(CSharpTypeDeclaration declaration : myTypeDeclarations)
		{
			if(declaration.isInheritor(typeDeclaration, b))
			{
				return true;
			}
		}
		return false;
	}

	@NotNull
	@RequiredReadAction
	@Override
	public DotNetTypeRef getTypeRefForEnumConstants()
	{
		throw new UnsupportedOperationException();
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		return myTypeDeclarations[0].getName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getVmQName()
	{
		return myTypeDeclarations[0].getVmQName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getVmName()
	{
		return myTypeDeclarations[0].getVmName();
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return myTypeDeclarations[0].getGenericParameters();
	}

	@Override
	public int getGenericParametersCount()
	{
		return myTypeDeclarations[0].getGenericParametersCount();
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		List<DotNetNamedElement> elements = new ArrayList<>();
		for(CSharpTypeDeclaration typeDeclaration : myTypeDeclarations)
		{
			Collections.addAll(elements, typeDeclaration.getMembers());
		}
		return ContainerUtil.toArray(elements, DotNetNamedElement.ARRAY_FACTORY);
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		// composite type dont hold partial modifier
		if(modifier == CSharpModifier.PARTIAL)
		{
			return false;
		}
		for(CSharpTypeDeclaration typeDeclaration : myTypeDeclarations)
		{
			if(typeDeclaration.hasModifier(modifier))
			{
				return true;
			}
		}
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myTypeDeclarations[0].getPresentableParentQName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myTypeDeclarations[0].getPresentableQName();
	}

	@Override
	public String toString()
	{
		return "CompositeTypeDeclaration: " + getVmQName();
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		for(CSharpTypeDeclaration typeDeclaration : myTypeDeclarations)
		{
			if(typeDeclaration.isEquivalentTo(another))
			{
				return true;
			}
		}
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		for(CSharpTypeDeclaration typeDeclaration : myTypeDeclarations)
		{
			typeDeclaration.setName(name);
		}
		return this;
	}

	@NotNull
	@Immutable
	public CSharpTypeDeclaration[] getTypeDeclarations()
	{
		return myTypeDeclarations;
	}
}
