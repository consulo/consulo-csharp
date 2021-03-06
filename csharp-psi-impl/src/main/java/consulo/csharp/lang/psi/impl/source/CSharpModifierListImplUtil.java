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

package consulo.csharp.lang.psi.impl.source;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.*;
import gnu.trove.THashSet;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpModifierListImplUtil
{
	private static final IElementType ourDummyUnregisteredModifier = new IElementType("ourDummyUnregisteredModifier", CSharpLanguage.INSTANCE);

	private static final Map<CSharpModifier, IElementType> ourModifiers = new LinkedHashMap<CSharpModifier, IElementType>()
	{
		{
			put(CSharpModifier.PUBLIC, CSharpTokens.PUBLIC_KEYWORD);
			put(CSharpModifier.PROTECTED, CSharpTokens.PROTECTED_KEYWORD);
			put(CSharpModifier.PRIVATE, CSharpTokens.PRIVATE_KEYWORD);
			put(CSharpModifier.STATIC, CSharpTokens.STATIC_KEYWORD);
			put(CSharpModifier.SEALED, CSharpTokens.SEALED_KEYWORD);
			put(CSharpModifier.ABSTRACT, CSharpTokens.ABSTRACT_KEYWORD);
			put(CSharpModifier.READONLY, CSharpTokens.READONLY_KEYWORD);
			put(CSharpModifier.UNSAFE, CSharpTokens.UNSAFE_KEYWORD);
			put(CSharpModifier.PARAMS, CSharpTokens.PARAMS_KEYWORD);
			put(CSharpModifier.THIS, CSharpTokens.THIS_KEYWORD);
			put(CSharpModifier.PARTIAL, CSharpSoftTokens.PARTIAL_KEYWORD);
			put(CSharpModifier.INTERNAL, CSharpTokens.INTERNAL_KEYWORD);
			put(CSharpModifier.REF, CSharpTokens.REF_KEYWORD);
			put(CSharpModifier.OUT, CSharpTokens.OUT_KEYWORD);
			put(CSharpModifier.VIRTUAL, CSharpTokens.VIRTUAL_KEYWORD);
			put(CSharpModifier.NEW, CSharpTokens.NEW_KEYWORD);
			put(CSharpModifier.OVERRIDE, CSharpTokens.OVERRIDE_KEYWORD);
			put(CSharpModifier.ASYNC, CSharpSoftTokens.ASYNC_KEYWORD);
			put(CSharpModifier.IN, CSharpSoftTokens.IN_KEYWORD);
			put(CSharpModifier.EXTERN, CSharpSoftTokens.EXTERN_KEYWORD);

			// this modifier stored in differed way
			put(CSharpModifier.INTERFACE_ABSTRACT, ourDummyUnregisteredModifier);
			put(CSharpModifier.OPTIONAL, ourDummyUnregisteredModifier);
		}
	};

	@Nonnull
	public static IElementType asElementType(@Nonnull DotNetModifier modifier)
	{
		CSharpModifier mod = CSharpModifier.as(modifier);
		return Objects.requireNonNull(ourModifiers.get(mod), "Unknown modifier: " + mod);
	}

	private static final EnumSet<CSharpModifier> emptySet = EnumSet.noneOf(CSharpModifier.class);

	@Nonnull
	@RequiredReadAction
	public static EnumSet<CSharpModifier> getModifiersCached(@Nonnull CSharpModifierList modifierList)
	{
		if(!modifierList.isValid())
		{
			return emptySet;
		}

		return CachedValuesManager.getCachedValue(modifierList, () ->
		{
			Set<CSharpModifier> modifiers = new THashSet<>();
			for(CSharpModifier modifier : CSharpModifier.values())
			{
				if(hasModifier(modifierList, modifier))
				{
					modifiers.add(modifier);
				}
			}
			return CachedValueProvider.Result.create(modifiers.isEmpty() ? emptySet : EnumSet.copyOf(modifiers), PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
		});
	}

	@RequiredReadAction
	public static boolean hasModifier(@Nonnull CSharpModifierList modifierList, @Nonnull DotNetModifier modifier)
	{
		if(modifierList.hasModifierInTree(modifier))
		{
			return true;
		}

		CSharpModifier cSharpModifier = CSharpModifier.as(modifier);
		PsiElement parent = modifierList.getParent();
		switch(cSharpModifier)
		{
			case PUBLIC:
				if(parent instanceof CSharpEnumConstantDeclaration)
				{
					return true;
				}
				if(parent instanceof DotNetVirtualImplementOwner && parent.getParent() instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) parent.getParent()).isInterface())
				{
					return true;
				}
				break;
			case READONLY:
				if(parent instanceof CSharpEnumConstantDeclaration)
				{
					return true;
				}
				break;
			case PRIVATE:
				if(parent instanceof CSharpTypeDeclaration && parent.getParent() instanceof CSharpTypeDeclaration)
				{
					if(findModifier(modifierList, cSharpModifier) == CSharpAccessModifier.NONE)
					{
						return true;
					}
				}
				break;
			case INTERNAL:
				if(parent instanceof CSharpTypeDeclaration && !(parent.getParent() instanceof CSharpTypeDeclaration))
				{
					if(findModifier(modifierList, cSharpModifier) == CSharpAccessModifier.NONE)
					{
						return true;
					}
				}
				break;
			case STATIC:
				if(parent instanceof CSharpFieldDeclaration)
				{
					if(((CSharpFieldDeclaration) parent).isConstant() && parent.getParent() instanceof CSharpTypeDeclaration)
					{
						return true;
					}
				}
				if(parent instanceof CSharpEnumConstantDeclaration)
				{
					return true;
				}
				if(parent instanceof DotNetXAccessor)
				{
					PsiElement superParent = parent.getParent();
					return superParent instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) superParent).hasModifier(DotNetModifier.STATIC);
				}
				break;
			case INTERFACE_ABSTRACT:
				if(parent instanceof DotNetVirtualImplementOwner && parent.getParent() instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) parent.getParent()).isInterface())
				{
					return true;
				}
				if(parent instanceof DotNetXAccessor)
				{
					CSharpCodeBodyProxy block = (CSharpCodeBodyProxy) ((DotNetXAccessor) parent).getCodeBlock();
					if(block.isSemicolonOrEmpty())
					{
						PsiElement accessorOwner = parent.getParent();
						if(accessorOwner instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) accessorOwner).hasModifier(modifier))
						{
							return true;
						}
					}
				}
				break;
			case ABSTRACT:
				if(parent instanceof DotNetTypeDeclaration && ((DotNetTypeDeclaration) parent).isInterface())
				{
					return true;
				}
				if(hasModifier(modifierList, CSharpModifier.INTERFACE_ABSTRACT))
				{
					return true;
				}
				if(parent instanceof DotNetXAccessor)
				{
					PsiElement superParent = parent.getParent();
					return superParent instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) superParent).hasModifier(DotNetModifier.ABSTRACT);
				}
				break;
			case SEALED:
				if(parent instanceof DotNetTypeDeclaration && (((DotNetTypeDeclaration) parent).isEnum() || ((DotNetTypeDeclaration) parent).isStruct()))
				{
					return true;
				}
				break;
		}
		return false;
	}

	@Nonnull
	@RequiredReadAction
	private static CSharpAccessModifier findModifier(@Nonnull CSharpModifierList list, CSharpModifier skipModifier)
	{
		loop:
		for(CSharpAccessModifier value : CSharpAccessModifier.VALUES)
		{
			if(value == CSharpAccessModifier.NONE)
			{
				continue;
			}

			if(value.getModifiers().length == 1 && value.getModifiers()[0] == skipModifier)
			{
				continue;
			}

			for(CSharpModifier modifier : value.getModifiers())
			{
				if(!hasModifier(list, modifier))
				{
					continue loop;
				}
			}
			return value;
		}
		return CSharpAccessModifier.NONE;
	}

	@RequiredReadAction
	public static void addModifier(@Nonnull CSharpModifierList modifierList, @Nonnull DotNetModifier modifier)
	{
		PsiElement anchor = modifierList.getLastChild();

		CSharpFieldDeclaration field = CSharpFileFactory.createField(modifierList.getProject(), modifier.getPresentableText() + " int b");
		PsiElement modifierElement = field.getModifierList().getModifierElement(modifier);

		PsiElement psiElement = modifierList.addAfter(modifierElement, anchor);
		modifierList.addAfter(PsiParserFacade.SERVICE.getInstance(modifierList.getProject()).createWhiteSpaceFromText(" "), psiElement);
	}

	public static void removeModifier(@Nonnull CSharpModifierList modifierList, @Nonnull DotNetModifier modifier)
	{
		CSharpModifier as = CSharpModifier.as(modifier);
		PsiElement modifierElement = modifierList.getModifierElement(as);
		if(modifierElement != null)
		{
			PsiElement next = modifierElement.getNextSibling();
			if(next instanceof PsiWhiteSpace)
			{
				next.delete();
			}

			modifierElement.delete();
		}
	}
}
