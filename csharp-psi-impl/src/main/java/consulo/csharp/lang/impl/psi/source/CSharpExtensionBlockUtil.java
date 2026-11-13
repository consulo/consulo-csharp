/*
 * Copyright 2013-2026 consulo.io
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
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightMethodDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.util.dataholder.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * C# 14 extension blocks as classic extension methods - {@code extension<T>(List<T> list) { public T First() }} is
 * {@code public static T First<T>(this List<T> list)} of the static class, so the extension method resolve applies
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CSharpExtensionBlockUtil {
    /**
     * the property of an extension block a getter-like extension method stands for - {@code s.Len} resolves to a property
     */
    public static final Key<CSharpPropertyDeclaration> EXTENSION_PROPERTY = Key.create("csharp.extension.block.property");

    private static final CSharpModifier[] COPIED_MODIFIERS = {CSharpModifier.PUBLIC, CSharpModifier.INTERNAL, CSharpModifier.PRIVATE, CSharpModifier.PROTECTED};

    /**
     * @return the classic extension methods of the instance methods of the extension blocks of the type
     */
    @RequiredReadAction
    public static List<CSharpMethodDeclaration> getExtensionMethods(DotNetTypeDeclaration typeDeclaration) {
        List<CSharpMethodDeclaration> result = new ArrayList<>();
        if (typeDeclaration instanceof CSharpCompositeTypeDeclaration composite) {
            for (CSharpTypeDeclaration part : composite.getTypeDeclarations()) {
                collect(part, result);
            }
        }
        else {
            collect(typeDeclaration, result);
        }
        return result;
    }

    @RequiredReadAction
    private static void collect(DotNetTypeDeclaration typeDeclaration, List<CSharpMethodDeclaration> result) {
        if (!(typeDeclaration instanceof CSharpTypeDeclarationImpl impl)) {
            return;
        }
        for (CSharpExtensionDeclarationImpl block : impl.getStubOrPsiChildren(CSharpStubElements.EXTENSION_DECLARATION, new CSharpExtensionDeclarationImpl[0])) {
            for (DotNetNamedElement member : block.getMembers()) {
                CSharpMethodDeclaration extension = null;
                if (member instanceof CSharpMethodDeclaration method) {
                    extension = toExtensionMethod(method);
                }
                else if (member instanceof CSharpPropertyDeclaration property) {
                    extension = toExtensionPropertyGetter(property);
                }
                if (extension != null) {
                    result.add(extension);
                }
            }
        }
    }

    /**
     * @return true for an instance method of an extension block with a named receiver - {@code extension(string s) { int Len() }}
     */
    @RequiredReadAction
    public static boolean isExtensionBlockInstanceMethod(CSharpMethodDeclaration method) {
        if (!(method.getParent() instanceof CSharpExtensionDeclarationImpl block) || method.hasModifier(CSharpModifier.STATIC) || method.isOperator()) {
            return false;
        }
        DotNetParameter receiver = block.getReceiverParameter();
        return receiver != null && receiver.getName() != null;
    }

    /**
     * @return the classic extension method of an instance method of an extension block, null for another method
     */
    @Nullable
    @RequiredReadAction
    public static CSharpMethodDeclaration toExtensionMethod(CSharpMethodDeclaration method) {
        if (!isExtensionBlockInstanceMethod(method)) {
            return null;
        }
        CSharpExtensionDeclarationImpl block = (CSharpExtensionDeclarationImpl) method.getParent();
        DotNetParameter receiver = block.getReceiverParameter();
        assert receiver != null;
        return LanguageCachedValueUtil.getCachedValue(method, () -> CachedValueProvider.Result.create(build(method, block, receiver), PsiModificationTracker.MODIFICATION_COUNT));
    }

    /**
     * @return a getter-like extension method of an instance property of an extension block - {@link #EXTENSION_PROPERTY}
     * is the property, null for another property
     */
    @Nullable
    @RequiredReadAction
    public static CSharpMethodDeclaration toExtensionPropertyGetter(CSharpPropertyDeclaration property) {
        if (!(property.getParent() instanceof CSharpExtensionDeclarationImpl block) || property.hasModifier(CSharpModifier.STATIC)) {
            return null;
        }
        DotNetParameter receiver = block.getReceiverParameter();
        if (receiver == null || receiver.getName() == null) {
            return null;
        }
        return LanguageCachedValueUtil.getCachedValue(property, () -> {
            CSharpLightMethodDeclarationBuilder builder = newExtensionBuilder(property, block, receiver, property.toTypeRef(false), property);
            builder.putUserData(EXTENSION_PROPERTY, property);
            return CachedValueProvider.Result.create(builder, PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

    /**
     * @return the extension block method a classic extension method is made of - null for another method
     */
    @Nullable
    public static CSharpMethodDeclaration getExtensionBlockMethod(PsiElement element) {
        if (element instanceof CSharpLightMethodDeclarationBuilder builder
            && builder.getNavigationElement() instanceof CSharpMethodDeclaration method
            && method.getParent() instanceof CSharpExtensionDeclarationImpl) {
            return method;
        }
        return null;
    }

    @RequiredReadAction
    private static CSharpMethodDeclaration build(CSharpMethodDeclaration method, CSharpExtensionDeclarationImpl block, DotNetParameter receiver) {
        CSharpLightMethodDeclarationBuilder builder = newExtensionBuilder(method, block, receiver, method.getReturnTypeRef(), null);
        for (DotNetParameter parameter : method.getParameters()) {
            builder.addParameter(parameter);
        }
        return builder;
    }

    @RequiredReadAction
    private static CSharpLightMethodDeclarationBuilder newExtensionBuilder(DotNetModifierListOwner member,
                                                                           CSharpExtensionDeclarationImpl block,
                                                                           DotNetParameter receiver,
                                                                           DotNetTypeRef returnTypeRef,
                                                                           @Nullable CSharpPropertyDeclaration property) {
        CSharpLightMethodDeclarationBuilder builder = new CSharpLightMethodDeclarationBuilder(member.getProject());
        builder.withName(String.valueOf(((PsiNamedElement) member).getName()));
        builder.withReturnType(returnTypeRef);
        builder.addModifier(CSharpModifier.STATIC);
        for (DotNetModifier modifier : COPIED_MODIFIERS) {
            if (member.hasModifier(modifier)) {
                builder.addModifier(modifier);
            }
        }
        // the type parameters of the block first - extension<T>(List<T> l) { U M<U>() } is M<T, U>(this List<T> l)
        for (DotNetGenericParameter genericParameter : block.getGenericParameters()) {
            builder.addGenericParameter(genericParameter);
        }
        if (property == null && member instanceof CSharpMethodDeclaration method) {
            for (DotNetGenericParameter genericParameter : method.getGenericParameters()) {
                builder.addGenericParameter(genericParameter);
            }
        }

        CSharpLightParameterBuilder thisParameter = new CSharpLightParameterBuilder(member.getProject());
        thisParameter.addModifier(CSharpModifier.THIS);
        thisParameter.withName(String.valueOf(receiver.getName()));
        thisParameter.withTypeRef(receiver.toTypeRef(false));
        thisParameter.setNavigationElement(receiver);
        builder.addParameter(thisParameter);

        PsiElement typeDeclaration = block.getParent();
        if (typeDeclaration instanceof DotNetTypeDeclaration type) {
            builder.withParent(type);
            builder.withParentQName(type.getPresentableQName());
        }
        builder.setNavigationElement(member);
        builder.withHashAndEqualObject(member);
        return builder;
    }
}
