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

package consulo.csharp.lang.impl.psi.msil;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.NullableLazyValue;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.light.CSharpLightAttributeBuilder;
import consulo.csharp.lang.impl.psi.light.CSharpLightAttributeWithSelfTypeBuilder;
import consulo.csharp.lang.impl.psi.source.CSharpModifierListImplUtil;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpModifierList;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.externalAttributes.ExternalAttributeArgumentNode;
import consulo.dotnet.externalAttributes.ExternalAttributeHolder;
import consulo.dotnet.externalAttributes.ExternalAttributeNode;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.impl.externalAttributes.ExternalAttributesUtil;
import consulo.language.psi.PsiElement;
import consulo.msil.impl.lang.psi.MsilTokens;
import consulo.msil.impl.lang.stubbing.MsilCustomAttributeStubber;
import consulo.msil.impl.lang.stubbing.values.MsiCustomAttributeValue;
import consulo.msil.lang.psi.MsilCustomAttribute;
import consulo.util.collection.ArrayUtil;

import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilModifierListToCSharpModifierList extends MsilElementWrapper<DotNetModifierList> implements CSharpModifierList {
    private static final String[] ourAttributeBans = new String[]{
            DotNetTypes.System.ParamArrayAttribute,
            DotNetTypes2.System.Runtime.CompilerServices.ParamCollectionAttribute,
            // C# itself here - dynamic, tuple names, in, ref struct, readonly struct, const decimal, indexers
            DotNetTypes2.System.Runtime.CompilerServices.DynamicAttribute,
            DotNetTypes2.System.Runtime.CompilerServices.TupleElementNamesAttribute,
            DotNetTypes2.System.Runtime.CompilerServices.IsReadOnlyAttribute,
            DotNetTypes2.System.Runtime.CompilerServices.RequiresLocationAttribute,
            DotNetTypes2.System.Runtime.CompilerServices.IsByRefLikeAttribute,
            DotNetTypes2.System.Runtime.CompilerServices.DecimalConstantAttribute,
            DotNetTypes2.System.Reflection.DefaultMemberAttribute,
            DotNetTypes.System.Runtime.CompilerServices.ExtensionAttribute,
            DotNetTypes2.System.Runtime.CompilerServices.AsyncStateMachineAttribute
    };

    /**
     * The features of {@code CompilerFeatureRequiredAttribute} C# here has - the ObsoleteAttribute next to it is for a compiler without them
     */
    private static final Set<String> ourKnownCompilerFeatures = Set.of("RefStructs", "RequiredMembers");

    private final DotNetModifierList myModifierList;

    private final CSharpModifier[] myAdditional;
    private List<DotNetAttribute> myAdditionalAttributes = Collections.emptyList();

    private NullableLazyValue<ExternalAttributeHolder> myAttributeHolderValue;

    private final Map<CSharpModifier, Boolean> myModifiersState = new ConcurrentHashMap<>();

    @RequiredReadAction
    public MsilModifierListToCSharpModifierList(PsiElement parent, DotNetModifierList modifierList) {
        this(CSharpModifier.EMPTY_ARRAY, parent, modifierList);
    }

    @RequiredReadAction
    public MsilModifierListToCSharpModifierList(CSharpModifier[] additional, PsiElement parent, DotNetModifierList modifierList) {
        super(parent, modifierList);
        myAdditional = additional;
        myModifierList = modifierList;

        if (myModifierList.hasModifier(MsilTokens.SERIALIZABLE_KEYWORD)) {
            addAdditionalAttribute(new CSharpLightAttributeBuilder(myModifierList, DotNetTypes.System.Serializable));
        }

        if (myModifierList.hasModifier(MsilTokens.BRACKET_OUT_KEYWORD)) {
            addAdditionalAttribute(new CSharpLightAttributeBuilder(myModifierList, DotNetTypes2.System.Runtime.InteropServices.OutAttribute));
        }

        if (myModifierList.hasModifier(MsilTokens.BRACKET_IN_KEYWORD)) {
            addAdditionalAttribute(new CSharpLightAttributeBuilder(myModifierList, DotNetTypes2.System.Runtime.InteropServices.InAttribute));
        }

        myAttributeHolderValue = NullableLazyValue.of(() -> ExternalAttributesUtil.findHolder(myModifierList));
    }

    public void addAdditionalAttribute(DotNetAttribute attribute) {
        if (myAdditionalAttributes.isEmpty()) {
            myAdditionalAttributes = new ArrayList<>(5);
        }
        myAdditionalAttributes.add(attribute);
    }

    @Override
    public void addModifier(DotNetModifier modifier) {

    }

    @Override
    public void accept(CSharpElementVisitor visitor) {
        visitor.visitModifierList(this);
    }

    @Override
    public void removeModifier(DotNetModifier modifier) {

    }

    @Override
    @RequiredReadAction
    public DotNetModifier[] getModifiers() {
        Set<CSharpModifier> all = new HashSet<>();
        for (CSharpModifier cSharpModifier : CSharpModifier.values()) {
            if (MsilToCSharpUtil.hasCSharpInMsilModifierList(cSharpModifier, myModifierList)) {
                all.add(cSharpModifier);
            }
        }
        Collections.addAll(all, myAdditional);
        return all.toArray(CSharpModifier[]::new);
    }

    @RequiredReadAction
    @Override
    public DotNetAttribute[] getAttributes() {
        DotNetAttribute[] oldAttributes = myModifierList.getAttributes();
        boolean knownFeatures = requiresKnownCompilerFeatures(oldAttributes);
        List<DotNetAttribute> attributes = new ArrayList<>(oldAttributes.length + myAdditionalAttributes.size());
        for (DotNetAttribute oldAttribute : oldAttributes) {
            DotNetTypeDeclaration resolvedType = oldAttribute.resolveToType();
            if (resolvedType != null && ArrayUtil.contains(resolvedType.getVmQName(), ourAttributeBans)) {
                continue;
            }
            // a ref struct, or a constructor of a type with required members, is obsolete for a compiler without the feature only
            if (knownFeatures && resolvedType != null && (DotNetTypes.System.ObsoleteAttribute.equals(resolvedType.getVmQName())
                    || DotNetTypes2.System.Runtime.CompilerServices.CompilerFeatureRequiredAttribute.equals(resolvedType.getVmQName()))) {
                continue;
            }
            attributes.add(oldAttribute);
        }
        attributes.addAll(myAdditionalAttributes);

        ExternalAttributeHolder holder = myAttributeHolderValue.getValue();

        if (holder != null) {
            List<ExternalAttributeNode> nodes = findAttributes(holder);
            for (ExternalAttributeNode node : nodes) {
                CSharpLightAttributeWithSelfTypeBuilder builder = new CSharpLightAttributeWithSelfTypeBuilder(myModifierList, node.getName());

                for (ExternalAttributeArgumentNode argumentNode : node.getArguments()) {
                    builder.addParameterExpression(argumentNode.toJavaObject());
                }
                attributes.add(builder);
            }
        }
        return attributes.toArray(new DotNetAttribute[attributes.size()]);
    }

    @RequiredReadAction
    private static boolean requiresKnownCompilerFeatures(DotNetAttribute[] attributes) {
        for (DotNetAttribute attribute : attributes) {
            if (attribute instanceof MsilCustomAttribute customAttribute
                    && DotNetTypes2.System.Runtime.CompilerServices.CompilerFeatureRequiredAttribute.equals(attribute.toTypeRef().getVmQName())) {
                List<MsiCustomAttributeValue> arguments = MsilCustomAttributeStubber.build(customAttribute).getConstructorArguments();
                if (arguments.size() == 1 && arguments.get(0).getValue() != null && ourKnownCompilerFeatures.contains(arguments.get(0).getValue().toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ExternalAttributeNode> findAttributes(ExternalAttributeHolder holder) {
        return Collections.emptyList();
    }

    @Override
    @RequiredReadAction
    public boolean hasModifier(DotNetModifier modifier) {
        return myModifiersState.computeIfAbsent(CSharpModifier.as(modifier), it -> CSharpModifierListImplUtil.hasModifier(this, it));
    }

    @Override
    public boolean hasModifierInTree(DotNetModifier modifier) {
        CSharpModifier cSharpModifier = CSharpModifier.as(modifier);
        if (ArrayUtil.contains(cSharpModifier, myAdditional)) {
            return true;
        }
        return MsilToCSharpUtil.hasCSharpInMsilModifierList(cSharpModifier, myModifierList);
    }

    @Nullable
    @Override
    public PsiElement getModifierElement(DotNetModifier modifier) {
        return null;
    }

    @Override
    public List<PsiElement> getModifierElements(DotNetModifier modifier) {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return myModifierList.toString();
    }

    @Override
    public CSharpAttributeList[] getAttributeLists() {
        return CSharpAttributeList.EMPTY_ARRAY;
    }
}
