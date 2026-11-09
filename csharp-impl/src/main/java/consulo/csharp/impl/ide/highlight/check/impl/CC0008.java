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
package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpFileUsages;
import consulo.csharp.lang.impl.psi.CSharpUseScopes;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.*;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.editor.inspection.reference.RefUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.language.psi.search.ReferencesSearch;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Private member is unused (Roslyn's IDE0051) - a private method, constructor with parameters, property, indexer or
 * event no reference of its type uses; a cref is not a use. The uses of the file are of {@link CSharpFileUsages}; the
 * parts of a partial type in other files are searched. A field is CS0169, a nested type is not looked at; nor what
 * the runtime or a designer calls: the members of a [StructLayout] type, serialization callbacks, event handlers,
 * ShouldSerializeX / ResetX of a property X, awaiter members, the deserialization constructor, a name of a
 * DebuggerDisplay
 *
 * @author VISTALL
 */
public class CC0008 extends CompilerCheck<DotNetModifierListOwner> {
    // called by the compiler, not by a name - a deconstruction is not a reference
    private static final Set<String> PATTERN_METHODS = Set.of("Deconstruct");

    private static final List<String> RUNTIME_CALLED_ATTRIBUTES = List.of(
        "System.Runtime.Serialization.OnDeserializingAttribute",
        "System.Runtime.Serialization.OnDeserializedAttribute",
        "System.Runtime.Serialization.OnSerializingAttribute",
        "System.Runtime.Serialization.OnSerializedAttribute",
        "System.Runtime.InteropServices.ComRegisterFunctionAttribute",
        "System.Runtime.InteropServices.ComUnregisterFunctionAttribute"
    );

    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetModifierListOwner element) {
        if (!(element.getParent() instanceof CSharpTypeDeclaration type) || !isChecked(element, type) || isUsed(element, type)) {
            return null;
        }
        PsiElement range = element instanceof CSharpIndexMethodDeclaration indexer ? CS0111.range(indexer) : getNameIdentifier(element);
        String name = element instanceof CSharpIndexMethodDeclaration ? "this" : ((DotNetNamedElement) element).getName();
        return newBuilder(range, type.getName() + "." + name);
    }

    @RequiredReadAction
    private static boolean isChecked(DotNetModifierListOwner element, CSharpTypeDeclaration type) {
        if (CSharpUseScopes.getPrivateMemberScope(element) == null) {
            return false;
        }
        if (DotNetAttributeUtil.hasAttribute(type, "System.Runtime.InteropServices.StructLayoutAttribute") || isInDebuggerDisplay(element, type)) {
            return false;
        }
        if (element instanceof CSharpMethodDeclaration method) {
            return !method.isDelegate() && !method.isOperator() && !method.hasModifier(CSharpModifier.PARTIAL) && !method.hasModifier(CSharpModifier.EXTERN)
                && !(method.hasModifier(CSharpModifier.STATIC) && "Main".equals(method.getName())) && !PATTERN_METHODS.contains(method.getName())
                && RUNTIME_CALLED_ATTRIBUTES.stream().noneMatch(attribute -> DotNetAttributeUtil.hasAttribute(method, attribute))
                && !isEventHandler(method) && !isDesignerMethod(method, type) && !isAwaiterMember(method.getName(), type);
        }
        if (element instanceof CSharpConstructorDeclaration constructor) {
            return !constructor.isDeConstructor() && !constructor.hasModifier(CSharpModifier.STATIC) && constructor.getParameters().length > 0
                && !isDeserializationConstructor(constructor);
        }
        if (element instanceof CSharpPropertyDeclaration property) {
            return !isAwaiterMember(property.getName(), type);
        }
        return element instanceof CSharpIndexMethodDeclaration || element instanceof CSharpEventDeclaration;
    }

    /**
     * (object sender, EventArgs e) - invoked by reflection (ASP.NET Page_Load and the like)
     */
    @RequiredReadAction
    private static boolean isEventHandler(CSharpMethodDeclaration method) {
        DotNetTypeRef[] parameters = method.getParameterTypeRefs();
        return parameters.length == 2 && DotNetTypeRefUtil.isVmQNameEqual(parameters[0], DotNetTypes.System.Object)
            && parameters[1].resolve().getElement() instanceof DotNetTypeDeclaration declaration && DotNetInheritUtil.isParentOrSelf("System.EventArgs", declaration, true);
    }

    /**
     * bool ShouldSerializeX() and void ResetX() of a property X - used by the designer property grid
     */
    @RequiredReadAction
    private static boolean isDesignerMethod(CSharpMethodDeclaration method, CSharpTypeDeclaration type) {
        String name = method.getName();
        if (name == null || method.getParameters().length != 0) {
            return false;
        }
        String property = name.startsWith("ShouldSerialize") ? name.substring("ShouldSerialize".length()) : name.startsWith("Reset") ? name.substring("Reset".length()) : "";
        if (property.isEmpty()) {
            return false;
        }
        for (DotNetNamedElement member : type.getMembers()) {
            if (member instanceof CSharpPropertyDeclaration && property.equals(member.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * GetAwaiter, GetResult, IsCompleted of an awaiter type
     */
    @RequiredReadAction
    private static boolean isAwaiterMember(@Nullable String name, CSharpTypeDeclaration type) {
        return ("GetAwaiter".equals(name) || "GetResult".equals(name) || "IsCompleted".equals(name))
            && type.isInheritor("System.Runtime.CompilerServices.INotifyCompletion", true);
    }

    /**
     * (SerializationInfo, StreamingContext) of an ISerializable type - invoked by the runtime
     */
    @RequiredReadAction
    private static boolean isDeserializationConstructor(CSharpConstructorDeclaration constructor) {
        DotNetTypeRef[] parameters = constructor.getParameterTypeRefs();
        return parameters.length == 2 && constructor.getParent() instanceof DotNetTypeDeclaration type && type.isInheritor("System.Runtime.Serialization.ISerializable", true)
            && DotNetTypeRefUtil.isVmQNameEqual(parameters[0], "System.Runtime.Serialization.SerializationInfo")
            && DotNetTypeRefUtil.isVmQNameEqual(parameters[1], "System.Runtime.Serialization.StreamingContext");
    }

    /**
     * The name is in a DebuggerDisplay of the type or of a member of it
     */
    @RequiredReadAction
    private static boolean isInDebuggerDisplay(DotNetModifierListOwner element, CSharpTypeDeclaration type) {
        String name = ((DotNetNamedElement) element).getName();
        if (name == null || !type.getText().contains("DebuggerDisplay")) {
            return false;
        }
        if (isInDebuggerDisplay(name, type)) {
            return true;
        }
        for (DotNetNamedElement member : type.getMembers()) {
            if (isInDebuggerDisplay(name, member)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static boolean isInDebuggerDisplay(String name, PsiElement owner) {
        DotNetAttribute attribute = DotNetAttributeUtil.findAttribute(owner, "System.Diagnostics.DebuggerDisplayAttribute");
        return attribute != null && attribute.getText().contains(name);
    }

    @RequiredReadAction
    private static boolean isUsed(DotNetModifierListOwner element, CSharpTypeDeclaration type) {
        if (CSharpFileUsages.isUsed(element) || RefUtil.isImplicitUsage(element)) {
            return true;
        }
        // the parts of other files
        CSharpCompositeTypeDeclaration compositeType = type.hasModifier(CSharpModifier.PARTIAL) ? CSharpCompositeTypeDeclaration.findCompositeType(type) : null;
        if (compositeType == null) {
            return false;
        }
        List<PsiElement> otherParts = new ArrayList<>();
        for (CSharpTypeDeclaration part : compositeType.getTypeDeclarations()) {
            if (part.getContainingFile() != element.getContainingFile()) {
                otherParts.add(part);
            }
        }
        return !otherParts.isEmpty() && ReferencesSearch.search(element, new LocalSearchScope(otherParts.toArray(PsiElement.EMPTY_ARRAY))).findFirst() != null;
    }
}
