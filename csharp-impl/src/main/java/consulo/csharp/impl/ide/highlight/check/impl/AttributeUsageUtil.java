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
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.impl.psi.CSharpPropertyUtil;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.source.CSharpBinaryExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpParenthesesExpressionImpl;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpEnumConstantDeclaration;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpNamedCallArgument;
import consulo.csharp.lang.psi.CSharpNamedFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetAttributeList;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.dotnet.psi.DotNetConstructorDeclaration;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.msil.impl.lang.stubbing.MsilCustomAttributeArgumentList;
import consulo.msil.impl.lang.stubbing.MsilCustomAttributeStubber;
import consulo.msil.impl.lang.stubbing.values.MsiCustomAttributeValue;
import consulo.msil.lang.psi.MsilCustomAttribute;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * The [AttributeUsage] of the attribute classes and the targets of the attribute lists - CS0579, CS0592, CS0657
 *
 * @author VISTALL
 * @since 2026-11-14
 */
class AttributeUsageUtil {
    // System.AttributeTargets
    static final int ASSEMBLY = 1, MODULE = 2, CLASS = 4, STRUCT = 8, ENUM = 16, CONSTRUCTOR = 32, METHOD = 64, PROPERTY = 128, FIELD = 256, EVENT = 512,
        INTERFACE = 1024, PARAMETER = 2048, DELEGATE = 4096, RETURN_VALUE = 8192, GENERIC_PARAMETER = 16384, ALL = 32767;

    private static final String ATTRIBUTE_USAGE = "System.AttributeUsageAttribute";

    record Usage(int validOn, boolean allowMultiple) {
        static final Usage DEFAULT = new Usage(ALL, false);
    }

    /**
     * @return the usage of an attribute class - of its [AttributeUsage] or of a base class, the attribute is inherited
     */
    @RequiredReadAction
    static Usage getUsage(DotNetTypeDeclaration attributeClass) {
        return LanguageCachedValueUtil.getCachedValue(attributeClass, () -> CachedValueProvider.Result.create(computeUsage(attributeClass), PsiModificationTracker.MODIFICATION_COUNT));
    }

    @RequiredReadAction
    private static Usage computeUsage(DotNetTypeDeclaration attributeClass) {
        DotNetTypeDeclaration type = attributeClass;
        for (int i = 0; type != null && i < 32; i++) {
            DotNetModifierList modifierList = type.getModifierList();
            if (modifierList != null) {
                for (DotNetAttribute attribute : modifierList.getAttributes()) {
                    DotNetTypeDeclaration attributeType = attribute.resolveToType();
                    if (attributeType != null && ATTRIBUTE_USAGE.equals(attributeType.getVmQName())) {
                        return toUsage(attribute);
                    }
                }
            }
            type = getBaseClass(type);
        }
        return Usage.DEFAULT;
    }

    @Nullable
    @RequiredReadAction
    private static DotNetTypeDeclaration getBaseClass(DotNetTypeDeclaration type) {
        for (DotNetTypeRef typeRef : type.getExtendTypeRefs()) {
            if (typeRef.resolve().getElement() instanceof DotNetTypeDeclaration base && !base.isInterface()) {
                return base;
            }
        }
        return null;
    }

    @RequiredReadAction
    private static Usage toUsage(DotNetAttribute attribute) {
        int validOn = ALL;
        boolean allowMultiple = false;
        if (attribute instanceof MsilCustomAttribute msilAttribute) {
            MsilCustomAttributeArgumentList arguments = MsilCustomAttributeStubber.build(msilAttribute);
            if (!arguments.getConstructorArguments().isEmpty() && arguments.getConstructorArguments().get(0).getValue() instanceof Number number) {
                validOn = number.intValue();
            }
            MsiCustomAttributeValue multiple = arguments.getNamedArguments().get("AllowMultiple");
            allowMultiple = multiple != null && Boolean.TRUE.equals(multiple.getValue());
        }
        else if (attribute instanceof CSharpAttribute csharpAttribute) {
            for (CSharpCallArgument argument : csharpAttribute.getCallArguments()) {
                DotNetExpression expression = argument.getArgumentExpression();
                if (expression == null) {
                    continue;
                }
                if (argument instanceof CSharpNamedCallArgument named) {
                    if ("AllowMultiple".equals(named.getName())) {
                        allowMultiple = Boolean.TRUE.equals(new ConstantExpressionEvaluator(expression).getValue());
                    }
                }
                else {
                    Integer targets = evaluateTargets(expression);
                    if (targets != null) {
                        validOn = targets;
                    }
                }
            }
            // AllowMultiple = true
            for (CSharpNamedFieldOrPropertySet set : PsiTreeUtil.findChildrenOfType(csharpAttribute, CSharpNamedFieldOrPropertySet.class)) {
                if ("AllowMultiple".equals(set.getNameElement().getReferenceName())) {
                    allowMultiple = Boolean.TRUE.equals(new ConstantExpressionEvaluator(set.getValueExpression()).getValue());
                }
            }
        }
        return new Usage(validOn, allowMultiple);
    }

    private static final Map<String, Integer> TARGET_NAMES = Map.ofEntries(Map.entry("Assembly", ASSEMBLY), Map.entry("Module", MODULE), Map.entry("Class", CLASS),
        Map.entry("Struct", STRUCT), Map.entry("Enum", ENUM), Map.entry("Constructor", CONSTRUCTOR), Map.entry("Method", METHOD), Map.entry("Property", PROPERTY),
        Map.entry("Field", FIELD), Map.entry("Event", EVENT), Map.entry("Interface", INTERFACE), Map.entry("Parameter", PARAMETER), Map.entry("Delegate", DELEGATE),
        Map.entry("ReturnValue", RETURN_VALUE), Map.entry("GenericParameter", GENERIC_PARAMETER), Map.entry("All", ALL));

    /**
     * @return the value of {@code AttributeTargets.Method | AttributeTargets.Class} - by the names, a library enum has no initializers
     */
    @Nullable
    @RequiredReadAction
    private static Integer evaluateTargets(@Nullable DotNetExpression expression) {
        if (expression instanceof CSharpParenthesesExpressionImpl parentheses) {
            return evaluateTargets(parentheses.getInnerExpression());
        }
        if (expression instanceof CSharpBinaryExpressionImpl binary && binary.getOperatorElement().getOperatorElementType() == CSharpTokens.OR) {
            Integer left = evaluateTargets(binary.getLeftExpression());
            Integer right = evaluateTargets(binary.getRightExpression());
            return left == null || right == null ? null : left | right;
        }
        if (expression instanceof CSharpReferenceExpression reference && TARGET_NAMES.containsKey(reference.getReferenceName())) {
            return TARGET_NAMES.get(reference.getReferenceName());
        }
        return new ConstantExpressionEvaluator(expression).getValue() instanceof Number number ? number.intValue() : null;
    }

    /**
     * @return the AttributeTargets of the attributes of a list - its explicit target or the default of its declaration, 0 when
     * unknown
     */
    @RequiredReadAction
    static int getTarget(DotNetAttributeList list) {
        PsiElement owner = getOwner(list);
        DotNetAttributeTargetType targetType = list.getTargetType();
        if (targetType != null) {
            return switch (targetType) {
                case ASSEMBLY -> ASSEMBLY;
                case MODULE -> MODULE;
                case FIELD -> FIELD;
                case EVENT -> EVENT;
                case METHOD -> METHOD;
                case PARAMETER -> PARAMETER;
                case PROPERTY -> PROPERTY;
                case RETURN -> RETURN_VALUE;
                case TYPE -> owner == null ? 0 : getDefaultTarget(owner);
                default -> 0;
            };
        }
        return owner == null ? 0 : getDefaultTarget(owner);
    }

    @RequiredReadAction
    static int getDefaultTarget(PsiElement owner) {
        if (owner instanceof DotNetTypeDeclaration type) {
            return type.isInterface() ? INTERFACE : type.isEnum() ? ENUM : type.isStruct() ? STRUCT : CLASS;
        }
        if (owner instanceof CSharpMethodDeclaration method && method.isDelegate()) {
            return DELEGATE;
        }
        if (owner instanceof CSharpConstructorDeclaration constructor) {
            return constructor.isDeConstructor() ? METHOD : CONSTRUCTOR;
        }
        if (owner instanceof DotNetLikeMethodDeclaration && !(owner instanceof CSharpIndexMethodDeclaration) || owner instanceof DotNetXAccessor) {
            return METHOD;
        }
        if (owner instanceof CSharpPropertyDeclaration || owner instanceof CSharpIndexMethodDeclaration) {
            return PROPERTY;
        }
        if (owner instanceof DotNetFieldDeclaration || owner instanceof CSharpEnumConstantDeclaration) {
            return FIELD;
        }
        if (owner instanceof CSharpEventDeclaration) {
            return EVENT;
        }
        if (owner instanceof DotNetParameter) {
            return PARAMETER;
        }
        if (owner instanceof DotNetGenericParameter) {
            return GENERIC_PARAMETER;
        }
        return 0;
    }

    /**
     * @return the valid explicit targets of the attribute lists of a declaration, null when unknown
     */
    @Nullable
    @RequiredReadAction
    static Set<DotNetAttributeTargetType> getValidTargets(PsiElement owner) {
        if (owner instanceof CSharpMethodDeclaration method && method.isDelegate()) {
            return EnumSet.of(DotNetAttributeTargetType.TYPE, DotNetAttributeTargetType.RETURN, DotNetAttributeTargetType.PARAMETER);
        }
        if (owner instanceof DotNetTypeDeclaration) {
            return EnumSet.of(DotNetAttributeTargetType.TYPE);
        }
        if (owner instanceof DotNetConstructorDeclaration) {
            return EnumSet.of(DotNetAttributeTargetType.METHOD);
        }
        if (owner instanceof CSharpIndexMethodDeclaration) {
            return EnumSet.of(DotNetAttributeTargetType.PROPERTY);
        }
        if (owner instanceof DotNetLikeMethodDeclaration) {
            return EnumSet.of(DotNetAttributeTargetType.METHOD, DotNetAttributeTargetType.RETURN);
        }
        if (owner instanceof DotNetXAccessor accessor) {
            return switch (accessor.getAccessorKind()) {
                case GET -> EnumSet.of(DotNetAttributeTargetType.METHOD, DotNetAttributeTargetType.RETURN);
                case ADD, REMOVE -> EnumSet.of(DotNetAttributeTargetType.METHOD, DotNetAttributeTargetType.PARAMETER);
                default -> EnumSet.of(DotNetAttributeTargetType.METHOD, DotNetAttributeTargetType.PARAMETER, DotNetAttributeTargetType.RETURN);
            };
        }
        if (owner instanceof CSharpPropertyDeclaration property) {
            // [field: X] of an auto property - C# 7.3
            return CSharpPropertyUtil.isAutoProperty(property) ? EnumSet.of(DotNetAttributeTargetType.PROPERTY, DotNetAttributeTargetType.FIELD)
                : EnumSet.of(DotNetAttributeTargetType.PROPERTY);
        }
        if (owner instanceof DotNetFieldDeclaration || owner instanceof CSharpEnumConstantDeclaration) {
            return EnumSet.of(DotNetAttributeTargetType.FIELD);
        }
        if (owner instanceof CSharpEventDeclaration event) {
            // a field-like event is a field and accessors too
            return event.getAccessors().length == 0 ? EnumSet.of(DotNetAttributeTargetType.EVENT, DotNetAttributeTargetType.FIELD, DotNetAttributeTargetType.METHOD)
                : EnumSet.of(DotNetAttributeTargetType.EVENT);
        }
        if (owner instanceof DotNetParameter) {
            return EnumSet.of(DotNetAttributeTargetType.PARAMETER);
        }
        return null;
    }

    /**
     * @return the declaration of an attribute list, null for [assembly: X] and [module: X]
     */
    @Nullable
    static PsiElement getOwner(DotNetAttributeList list) {
        PsiElement parent = list.getParent();
        return parent instanceof DotNetModifierList ? parent.getParent() : null;
    }

    @Nullable
    static PsiElement getTargetElement(DotNetAttributeList list) {
        ASTNode node = list.getNode().findChildByType(CSharpTokenSets.ATTRIBUTE_TARGETS);
        return node == null ? null : node.getPsi();
    }
}
