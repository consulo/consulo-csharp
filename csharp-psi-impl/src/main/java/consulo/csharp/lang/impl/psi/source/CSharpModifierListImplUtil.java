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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.*;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.PsiParserFacade;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.LanguageCachedValueUtil;
import jakarta.annotation.Nonnull;

import java.util.*;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpModifierListImplUtil {
    private static final IElementType ourDummyUnregisteredModifier = new IElementType("ourDummyUnregisteredModifier", CSharpLanguage.INSTANCE);

    private static final Map<CSharpModifier, IElementType> ourModifiers = new LinkedHashMap<>();

    static {
        ourModifiers.put(CSharpModifier.PUBLIC, CSharpTokens.PUBLIC_KEYWORD);
        ourModifiers.put(CSharpModifier.PROTECTED, CSharpTokens.PROTECTED_KEYWORD);
        ourModifiers.put(CSharpModifier.PRIVATE, CSharpTokens.PRIVATE_KEYWORD);
        ourModifiers.put(CSharpModifier.STATIC, CSharpTokens.STATIC_KEYWORD);
        ourModifiers.put(CSharpModifier.SEALED, CSharpTokens.SEALED_KEYWORD);
        ourModifiers.put(CSharpModifier.ABSTRACT, CSharpTokens.ABSTRACT_KEYWORD);
        ourModifiers.put(CSharpModifier.READONLY, CSharpTokens.READONLY_KEYWORD);
        ourModifiers.put(CSharpModifier.UNSAFE, CSharpTokens.UNSAFE_KEYWORD);
        ourModifiers.put(CSharpModifier.PARAMS, CSharpTokens.PARAMS_KEYWORD);
        ourModifiers.put(CSharpModifier.THIS, CSharpTokens.THIS_KEYWORD);
        ourModifiers.put(CSharpModifier.PARTIAL, CSharpSoftTokens.PARTIAL_KEYWORD);
        ourModifiers.put(CSharpModifier.INTERNAL, CSharpTokens.INTERNAL_KEYWORD);
        ourModifiers.put(CSharpModifier.REF, CSharpTokens.REF_KEYWORD);
        ourModifiers.put(CSharpModifier.OUT, CSharpTokens.OUT_KEYWORD);
        ourModifiers.put(CSharpModifier.VIRTUAL, CSharpTokens.VIRTUAL_KEYWORD);
        ourModifiers.put(CSharpModifier.NEW, CSharpTokens.NEW_KEYWORD);
        ourModifiers.put(CSharpModifier.OVERRIDE, CSharpTokens.OVERRIDE_KEYWORD);
        ourModifiers.put(CSharpModifier.ASYNC, CSharpSoftTokens.ASYNC_KEYWORD);
        ourModifiers.put(CSharpModifier.IN, CSharpSoftTokens.IN_KEYWORD);
        ourModifiers.put(CSharpModifier.EXTERN, CSharpSoftTokens.EXTERN_KEYWORD);
        ourModifiers.put(CSharpModifier.FIXED, CSharpSoftTokens.FIXED_KEYWORD);

        // this modifier stored in differed way
        ourModifiers.put(CSharpModifier.INTERFACE_ABSTRACT, ourDummyUnregisteredModifier);
        ourModifiers.put(CSharpModifier.OPTIONAL, ourDummyUnregisteredModifier);
    }

    @Nonnull
    public static IElementType asElementType(@Nonnull DotNetModifier modifier) {
        CSharpModifier mod = CSharpModifier.as(modifier);
        return Objects.requireNonNull(ourModifiers.get(mod), "Unknown modifier: " + mod);
    }

    private static final EnumSet<CSharpModifier> emptySet = EnumSet.noneOf(CSharpModifier.class);

    @Nonnull
    @RequiredReadAction
    public static EnumSet<CSharpModifier> getModifiersCached(@Nonnull CSharpModifierList modifierList) {
        if (!modifierList.isValid()) {
            return emptySet;
        }

        return LanguageCachedValueUtil.getCachedValue(modifierList, () ->
        {
            Set<CSharpModifier> modifiers = new HashSet<>();
            for (CSharpModifier modifier : CSharpModifier.values()) {
                if (hasModifier(modifierList, modifier)) {
                    modifiers.add(modifier);
                }
            }
            return CachedValueProvider.Result.create(modifiers.isEmpty() ? emptySet : EnumSet.copyOf(modifiers), PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

    @RequiredReadAction
    public static boolean hasModifier(@Nonnull CSharpModifierList modifierList, @Nonnull DotNetModifier modifier) {
        if (modifierList.hasModifierInTree(modifier)) {
            return true;
        }

        CSharpModifier cSharpModifier = CSharpModifier.as(modifier);
        PsiElement parent = modifierList.getParent();
        switch (cSharpModifier) {
            case PUBLIC:
                if (parent instanceof CSharpEnumConstantDeclaration) {
                    return true;
                }
                if (parent instanceof DotNetVirtualImplementOwner && parent.getParent() instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) parent.getParent()).isInterface()) {
                    return true;
                }
                break;
            case READONLY:
                if (parent instanceof CSharpEnumConstantDeclaration) {
                    return true;
                }
                break;
            case PRIVATE:
                if (parent instanceof CSharpTypeDeclaration && parent.getParent() instanceof CSharpTypeDeclaration) {
                    if (findModifier(modifierList, cSharpModifier) == CSharpAccessModifier.NONE) {
                        return true;
                    }
                }
                break;
            case INTERNAL:
                if (parent instanceof CSharpTypeDeclaration && !(parent.getParent() instanceof CSharpTypeDeclaration)) {
                    if (findModifier(modifierList, cSharpModifier) == CSharpAccessModifier.NONE) {
                        return true;
                    }
                }
                break;
            case STATIC:
                if (parent instanceof CSharpFieldDeclaration) {
                    if (((CSharpFieldDeclaration) parent).isConstant() && parent.getParent() instanceof CSharpTypeDeclaration) {
                        return true;
                    }
                }
                if (parent instanceof CSharpEnumConstantDeclaration) {
                    return true;
                }
                if (parent instanceof DotNetXAccessor) {
                    PsiElement superParent = parent.getParent();
                    return superParent instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) superParent).hasModifier(DotNetModifier.STATIC);
                }
                break;
            case INTERFACE_ABSTRACT:
                if (parent instanceof DotNetVirtualImplementOwner && parent.getParent() instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) parent.getParent()).isInterface()) {
                    return true;
                }
                if (parent instanceof DotNetXAccessor) {
                    CSharpCodeBodyProxy block = (CSharpCodeBodyProxy) ((DotNetXAccessor) parent).getCodeBlock();
                    if (block.isSemicolonOrEmpty()) {
                        PsiElement accessorOwner = parent.getParent();
                        if (accessorOwner instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) accessorOwner).hasModifier(modifier)) {
                            return true;
                        }
                    }
                }
                break;
            case ABSTRACT:
                if (parent instanceof DotNetTypeDeclaration && ((DotNetTypeDeclaration) parent).isInterface()) {
                    return true;
                }
                if (hasModifier(modifierList, CSharpModifier.INTERFACE_ABSTRACT)) {
                    return true;
                }
                if (parent instanceof DotNetXAccessor) {
                    PsiElement superParent = parent.getParent();
                    return superParent instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) superParent).hasModifier(DotNetModifier.ABSTRACT);
                }
                break;
            case SEALED:
                if (parent instanceof DotNetTypeDeclaration && (((DotNetTypeDeclaration) parent).isEnum() || ((DotNetTypeDeclaration) parent).isStruct())) {
                    return true;
                }
                break;
        }
        return false;
    }

    @Nonnull
    @RequiredReadAction
    private static CSharpAccessModifier findModifier(@Nonnull CSharpModifierList list, CSharpModifier skipModifier) {
        loop:
        for (CSharpAccessModifier value : CSharpAccessModifier.VALUES) {
            if (value == CSharpAccessModifier.NONE) {
                continue;
            }

            if (value.getModifiers().length == 1 && value.getModifiers()[0] == skipModifier) {
                continue;
            }

            for (CSharpModifier modifier : value.getModifiers()) {
                if (!hasModifier(list, modifier)) {
                    continue loop;
                }
            }
            return value;
        }
        return CSharpAccessModifier.NONE;
    }

    @RequiredReadAction
    public static void addModifier(@Nonnull CSharpModifierList modifierList, @Nonnull DotNetModifier modifier) {
        PsiElement anchor = modifierList.getLastChild();

        CSharpFieldDeclaration field = CSharpFileFactory.createField(modifierList.getProject(), modifier.getPresentableText() + " int b");
        PsiElement modifierElement = field.getModifierList().getModifierElement(modifier);

        PsiElement psiElement = modifierList.addAfter(modifierElement, anchor);
        modifierList.addAfter(PsiParserFacade.SERVICE.getInstance(modifierList.getProject()).createWhiteSpaceFromText(" "), psiElement);
    }

    public static void removeModifier(@Nonnull CSharpModifierList modifierList, @Nonnull DotNetModifier modifier) {
        CSharpModifier as = CSharpModifier.as(modifier);
        PsiElement modifierElement = modifierList.getModifierElement(as);
        if (modifierElement != null) {
            PsiElement next = modifierElement.getNextSibling();
            if (next instanceof PsiWhiteSpace) {
                next.delete();
            }

            modifierElement.delete();
        }
    }
}
