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

package consulo.csharp.impl.ide.highlight.check.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.annotation.Nonnull;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ArrayUtil;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CS0106 extends CompilerCheck<DotNetModifierListOwner>
{
	public static enum Owners
	{
		Constructor(CSharpModifier.PUBLIC, CSharpModifier.PRIVATE, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL),
		StaticConstructor(CSharpModifier.STATIC),
		Constant(CSharpModifier.PUBLIC, CSharpModifier.PRIVATE, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL),
		DeConstructor,
		InterfaceMember(CSharpModifier.NEW),
		GenericParameter(CSharpModifier.IN, CSharpModifier.OUT),
		Parameter(CSharpModifier.REF, CSharpModifier.OUT, CSharpModifier.PARAMS, CSharpModifier.THIS),
		NamespaceStruct(CSharpModifier.PUBLIC, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL),
		NestedStruct(CSharpModifier.PUBLIC, CSharpModifier.PROTECTED, CSharpModifier.PRIVATE, CSharpModifier.INTERNAL),
		NamespaceType(CSharpModifier.STATIC, CSharpModifier.PUBLIC, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL, CSharpModifier.ABSTRACT, CSharpModifier.PARTIAL, CSharpModifier.SEALED),
		NestedType(CSharpModifier.PUBLIC, CSharpModifier.PRIVATE, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL, CSharpModifier.ABSTRACT, CSharpModifier.PARTIAL, CSharpModifier.SEALED,
				CSharpModifier.STATIC, CSharpModifier.NEW),
		Unknown
				{
					@Override
					public boolean isValidModifier(DotNetModifier modifier)
					{
						return true;
					}
				};

		private DotNetModifier[] myValidModifiers;

		Owners(DotNetModifier... validModifiers)
		{
			myValidModifiers = validModifiers;
		}

		public boolean isValidModifier(DotNetModifier modifier)
		{
			return ArrayUtil.contains(modifier, myValidModifiers);
		}
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public List<CompilerCheckBuilder> check(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetModifierListOwner element)
	{
		DotNetModifierList modifierList = element.getModifierList();
		if(modifierList == null)
		{
			return Collections.emptyList();
		}

		List<CompilerCheckBuilder> list = Collections.emptyList();
		Owners owners = toOwners(element);

		DotNetModifier[] modifiers = modifierList.getModifiers();
		if(modifiers.length == 0)
		{
			return list;
		}

		for(DotNetModifier modifier : modifiers)
		{
			if(!owners.isValidModifier(modifier))
			{
				PsiElement modifierElement = modifierList.getModifierElement(modifier);
				if(modifierElement == null)
				{
					continue;
				}

				if(list.isEmpty())
				{
					list = new ArrayList<>(2);
				}

				list.add(newBuilder(modifierElement, modifier.getPresentableText()).withQuickFix(new RemoveModifierFix(modifier, element)));
			}
		}
		return list;
	}

	@RequiredReadAction
	public static Owners toOwners(DotNetModifierListOwner owner)
	{
		if(owner instanceof CSharpFieldDeclaration && ((CSharpFieldDeclaration) owner).isConstant())
		{
			return Owners.Constant;
		}

		if(owner instanceof CSharpConstructorDeclaration)
		{
			if(((CSharpConstructorDeclaration) owner).isDeConstructor())
			{
				return Owners.DeConstructor;
			}

			if(owner.hasModifier(DotNetModifier.STATIC))
			{
				return Owners.StaticConstructor;
			}
			return Owners.Constructor;
		}

		if(owner instanceof CSharpMethodDeclaration || owner instanceof CSharpPropertyDeclaration || owner instanceof CSharpIndexMethodDeclaration)
		{
			PsiElement parent = owner.getParent();

			if(parent instanceof CSharpTypeDeclaration)
			{
				if(((CSharpTypeDeclaration) parent).isInterface())
				{
					return Owners.InterfaceMember;
				}
			}
		}

		if(owner instanceof DotNetGenericParameter)
		{
			return Owners.GenericParameter;
		}

		if(owner instanceof DotNetTypeDeclaration)
		{
			boolean struct = ((DotNetTypeDeclaration) owner).isEnum() || ((DotNetTypeDeclaration) owner).isStruct();

			PsiElement parent = owner.getParent();
			if(parent instanceof CSharpNamespaceDeclaration || parent instanceof CSharpFile)
			{
				return struct ? Owners.NamespaceStruct : Owners.NamespaceType;
			}
			else if(parent instanceof DotNetTypeDeclaration)
			{
				return struct ? Owners.NestedStruct : Owners.NestedType;
			}
		}

		if(owner instanceof DotNetParameter)
		{
			return Owners.Parameter;
		}
		return Owners.Unknown;
	}
}
