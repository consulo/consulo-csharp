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
package consulo.csharp.lang.impl.psi.interop;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightConstructorDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightFieldDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightMethodDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightNamespaceDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightPropertyDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightTypeDeclarationBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetConstructorDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetArrayTypeRef;
import consulo.dotnet.psi.resolve.DotNetGenericWrapperTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Wraps types of other .NET languages into C# light PSI, one cached wrapper per top level type.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
public final class InteropToCSharpUtil {
    private static final List<DotNetModifier> MODIFIERS = List.of(DotNetModifier.PUBLIC, DotNetModifier.PRIVATE, DotNetModifier.PROTECTED,
        DotNetModifier.INTERNAL, DotNetModifier.STATIC, DotNetModifier.ABSTRACT, DotNetModifier.SEALED);

    private InteropToCSharpUtil() {
    }

    /**
     * @return the wrapper of {@code type}; a nested type is found in the wrapper of its outermost type
     */
    @RequiredReadAction
    public static @Nullable PsiElement wrap(DotNetTypeDeclaration type) {
        DotNetTypeDeclaration root = type;
        while (root.getParent() instanceof DotNetTypeDeclaration outer) {
            root = outer;
        }
        DotNetTypeDeclaration rootType = root;
        InteropTypeAsCSharpTypeDeclaration wrapper = LanguageCachedValueUtil.getCachedValue(rootType,
            () -> CachedValueProvider.Result.create(build(rootType, null), PsiModificationTracker.MODIFICATION_COUNT));
        if (root == type) {
            return wrapper;
        }
        return findByOriginal(wrapper, type);
    }

    private static @Nullable PsiElement findByOriginal(InteropTypeAsCSharpTypeDeclaration wrapper, DotNetTypeDeclaration original) {
        if (wrapper.getOriginal().equals(original)) {
            return wrapper;
        }
        for (DotNetNamedElement member : wrapper.getMembers()) {
            if (member instanceof InteropTypeAsCSharpTypeDeclaration nested) {
                PsiElement found = findByOriginal(nested, original);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @RequiredReadAction
    private static InteropTypeAsCSharpTypeDeclaration build(DotNetTypeDeclaration type, @Nullable InteropTypeAsCSharpTypeDeclaration outer) {
        Project project = type.getProject();
        GlobalSearchScope scope = type.getResolveScope();
        InteropTypeAsCSharpTypeDeclaration wrapper = new InteropTypeAsCSharpTypeDeclaration(type, scope);
        wrapper.withName(compiledName(type));
        if (outer != null) {
            wrapper.withParent(outer);
        }
        else {
            String parentQName = type.getPresentableParentQName();
            if (parentQName != null && !parentQName.isEmpty()) {
                wrapper.withParent(new CSharpLightNamespaceDeclarationBuilder(project, parentQName));
            }
            wrapper.withParentQName(parentQName == null ? "" : parentQName);
        }
        for (DotNetModifier modifier : MODIFIERS) {
            if (type.hasModifier(modifier)) {
                wrapper.addModifier(modifier);
            }
        }
        if (type.isInterface()) {
            wrapper.withType(CSharpLightTypeDeclarationBuilder.Type.INTERFACE);
        }
        else if (type.isEnum()) {
            wrapper.withType(CSharpLightTypeDeclarationBuilder.Type.ENUM);
        }
        else if (type.isStruct()) {
            wrapper.withType(CSharpLightTypeDeclarationBuilder.Type.STRUCT);
        }
        for (DotNetTypeRef extendTypeRef : type.getExtendTypeRefs()) {
            wrapper.addExtendType(toCSharp(project, scope, extendTypeRef));
        }
        for (DotNetNamedElement member : type.getMembers()) {
            DotNetQualifiedElement wrapped = wrapMember(project, scope, wrapper, member);
            if (wrapped != null) {
                wrapper.addMember(wrapped);
            }
        }
        return wrapper;
    }

    /**
     * The name other languages use: the VM name without the generic marker.
     */
    @RequiredReadAction
    private static String compiledName(DotNetTypeDeclaration type) {
        String vmName = type.getVmName();
        if (vmName == null) {
            return String.valueOf(type.getName());
        }
        int marker = vmName.indexOf('\u0060');
        return marker < 0 ? vmName : vmName.substring(0, marker);
    }

    @RequiredReadAction
    private static @Nullable DotNetQualifiedElement wrapMember(Project project, GlobalSearchScope scope, InteropTypeAsCSharpTypeDeclaration owner,
                                                               DotNetNamedElement member) {
        if (member instanceof DotNetTypeDeclaration nested) {
            return build(nested, owner);
        }
        if (member instanceof DotNetConstructorDeclaration constructor) {
            CSharpLightConstructorDeclarationBuilder builder = new CSharpLightConstructorDeclarationBuilder(owner);
            builder.withName(String.valueOf(owner.getName()));
            addModifiers(builder::addModifier, constructor);
            addParameters(project, scope, builder::addParameter, constructor, false);
            builder.setNavigationElement(constructor);
            builder.withHashAndEqualObject(constructor);
            return builder;
        }
        if (member instanceof DotNetLikeMethodDeclaration method) {
            CSharpLightMethodDeclarationBuilder builder = new CSharpLightMethodDeclarationBuilder(project);
            builder.withName(String.valueOf(method.getName()));
            builder.withReturnType(toCSharp(project, scope, method.getReturnTypeRef()));
            addModifiers(builder::addModifier, method);
            addParameters(project, scope, builder::addParameter, method,
                DotNetAttributeUtil.hasAttribute(method, DotNetTypes.System.Runtime.CompilerServices.ExtensionAttribute));
            builder.setNavigationElement(method);
            builder.withHashAndEqualObject(method);
            return builder;
        }
        if (member instanceof DotNetPropertyDeclaration property) {
            CSharpLightPropertyDeclarationBuilder builder = new CSharpLightPropertyDeclarationBuilder(project);
            builder.withName(String.valueOf(property.getName()));
            builder.withTypeRef(toCSharp(project, scope, property.toTypeRef(false)));
            addModifiers(modifier -> builder.addModifier(CSharpModifier.as(modifier)), property);
            builder.setNavigationElement(property);
            builder.withHashAndEqualObject(property);
            return builder;
        }
        if (member instanceof DotNetFieldDeclaration field) {
            CSharpLightFieldDeclarationBuilder builder = new CSharpLightFieldDeclarationBuilder(project);
            builder.withName(String.valueOf(field.getName()));
            builder.withTypeRef(toCSharp(project, scope, field.toTypeRef(false)));
            addModifiers(modifier -> builder.addModifier(CSharpModifier.as(modifier)), field);
            if (field.isConstant()) {
                builder.setConstant();
            }
            builder.setNavigationElement(field);
            builder.withHashAndEqualObject(field);
            return builder;
        }
        return null;
    }

    private interface ModifierSink {
        void add(DotNetModifier modifier);
    }

    private interface ParameterSink {
        void add(DotNetParameter parameter);
    }

    @RequiredReadAction
    private static void addModifiers(ModifierSink sink, DotNetModifierListOwner owner) {
        for (DotNetModifier modifier : MODIFIERS) {
            if (owner.hasModifier(modifier)) {
                sink.add(modifier);
            }
        }
    }

    /**
     * @param extension the first parameter of an extension method is {@code this} for C#
     */
    @RequiredReadAction
    private static void addParameters(Project project, GlobalSearchScope scope, ParameterSink sink, DotNetLikeMethodDeclaration method,
                                      boolean extension) {
        DotNetParameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            DotNetParameter parameter = parameters[i];
            CSharpLightParameterBuilder builder = new CSharpLightParameterBuilder(project);
            if (extension && i == 0) {
                builder.addModifier(CSharpModifier.THIS);
            }
            builder.withName(String.valueOf(parameter.getName()));
            builder.withTypeRef(toCSharp(project, scope, parameter.toTypeRef(false)));
            builder.setNavigationElement(parameter);
            sink.add(builder);
        }
    }

    /**
     * Type references of other languages resolve to their own PSI: as C# references they resolve to the wrappers.
     */
    @RequiredReadAction
    public static DotNetTypeRef toCSharp(Project project, GlobalSearchScope scope, DotNetTypeRef typeRef) {
        if (typeRef == DotNetTypeRef.UNKNOWN_TYPE || typeRef == DotNetTypeRef.ERROR_TYPE) {
            return typeRef;
        }
        if (typeRef instanceof DotNetArrayTypeRef array) {
            return new CSharpArrayTypeRef(project, scope, toCSharp(project, scope, array.getInnerTypeRef()), 0);
        }
        if (typeRef instanceof DotNetGenericWrapperTypeRef generic) {
            DotNetTypeRef[] arguments = generic.getArgumentTypeRefs();
            DotNetTypeRef[] converted = new DotNetTypeRef[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                converted[i] = toCSharp(project, scope, arguments[i]);
            }
            return new CSharpGenericWrapperTypeRef(project, scope, toCSharp(project, scope, generic.getInnerTypeRef()), converted);
        }
        String vmQName = typeRef.getVmQName();
        if (vmQName == null || vmQName.startsWith("__")) {
            return typeRef;
        }
        return new CSharpTypeRefByQName(project, scope, vmQName);
    }
}
