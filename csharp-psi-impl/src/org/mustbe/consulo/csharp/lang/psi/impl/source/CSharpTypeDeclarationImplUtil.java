/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightConstructorDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpTypeResolveContextImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpTypeResolveContext;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CSharpTypeDeclarationImplUtil
{
	private static final Key<CachedValue<CSharpTypeResolveContext>> TYPE_RESOLVE_CONTEXT = Key.create("type-resolve-context");

	@NotNull
	public static CSharpTypeResolveContext getOrCreateResolveContext(@NotNull final CSharpTypeDeclaration typeDeclaration)
	{
		CachedValue<CSharpTypeResolveContext> provider = typeDeclaration.getUserData(TYPE_RESOLVE_CONTEXT);
		if(provider != null)
		{
			return provider.getValue();
		}

		CachedValuesManager.getManager(typeDeclaration.getProject()).createCachedValue(new CachedValueProvider<CSharpTypeResolveContext>()
		{
			@Nullable
			@Override
			public Result<CSharpTypeResolveContext> compute()
			{
				return Result.<CSharpTypeResolveContext>create(new CSharpTypeResolveContextImpl(typeDeclaration),
						PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
			}
		});
		return new CSharpTypeResolveContextImpl(typeDeclaration);
	}

	public static boolean hasExtensions(@NotNull DotNetTypeDeclaration typeDeclaration)
	{
		for(DotNetNamedElement qualifiedElement : typeDeclaration.getMembers())
		{
			if(CSharpMethodImplUtil.isExtensionMethod(qualifiedElement))
			{
				return true;
			}
		}
		return false;
	}

	@NotNull
	public static DotNetTypeRef[] getExtendTypeRefs(@NotNull DotNetTypeDeclaration t)
	{
		DotNetTypeRef[] typeRefs = DotNetTypeRef.EMPTY_ARRAY;
		DotNetTypeList extendList = t.getExtendList();
		if(extendList != null && !t.isEnum())
		{
			typeRefs = extendList.getTypeRefs();
		}

		if(typeRefs.length == 0)
		{
			String defaultSuperType = getDefaultSuperType(t);
			if(defaultSuperType == null)
			{
				return DotNetTypeRef.EMPTY_ARRAY;
			}
			typeRefs = new DotNetTypeRef[]{new DotNetTypeRefByQName(defaultSuperType, CSharpTransform.INSTANCE)};
		}
		return typeRefs;
	}

	@NotNull
	public static DotNetTypeRef resolveBaseTypeRef(@NotNull DotNetTypeDeclaration typeDeclaration, @NotNull PsiElement scope)
	{
		DotNetTypeRef[] anExtends = typeDeclaration.getExtendTypeRefs();
		if(anExtends.length == 0)
		{
			return new DotNetTypeRefByQName(DotNetTypes.System.Object, CSharpTransform.INSTANCE);
		}
		else
		{
			for(DotNetTypeRef anExtend : anExtends)
			{
				PsiElement resolve = anExtend.resolve(scope);
				if(resolve instanceof DotNetTypeDeclaration && !((DotNetTypeDeclaration) resolve).isInterface())
				{
					return anExtend;
				}
			}

			return new DotNetTypeRefByQName(DotNetTypes.System.Object, CSharpTransform.INSTANCE);
		}
	}

	@Nullable
	public static String getDefaultSuperType(@NotNull DotNetTypeDeclaration typeDeclaration)
	{
		String presentableQName = typeDeclaration.getPresentableQName();
		if(Comparing.equal(presentableQName, DotNetTypes.System.Object))
		{
			return null;
		}
		if(typeDeclaration.isStruct())
		{
			return DotNetTypes.System.ValueType;
		}
		else if(typeDeclaration.isEnum())
		{
			return DotNetTypes.System.Enum;
		}
		else
		{
			return DotNetTypes.System.Object;
		}
	}

	public static void processConstructors(@NotNull DotNetTypeDeclaration type, @NotNull Processor<DotNetConstructorDeclaration> processor)
	{
		for(DotNetNamedElement dotNetNamedElement : type.getMembers())
		{
			if(!(dotNetNamedElement instanceof DotNetConstructorDeclaration))
			{
				continue;
			}

			if(!processor.process((DotNetConstructorDeclaration) dotNetNamedElement))
			{
				return;
			}
		}

		if(type.isStruct())
		{
			CSharpLightConstructorDeclarationBuilder builder = new CSharpLightConstructorDeclarationBuilder(type.getProject());
			builder.addModifier(CSharpModifier.PUBLIC);
			builder.setNavigationElement(type);
			builder.withParent(type);
			builder.withName(type.getName());

			processor.process(builder);
		}
	}
}
