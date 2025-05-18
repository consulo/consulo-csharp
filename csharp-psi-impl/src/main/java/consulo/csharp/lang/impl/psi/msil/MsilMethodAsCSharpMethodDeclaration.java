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
import consulo.csharp.lang.impl.psi.msil.typeParsing.SomeType;
import consulo.csharp.lang.impl.psi.msil.typeParsing.SomeTypeParser;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpMethodImplUtil;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.msil.lang.psi.MsilClassEntry;
import consulo.msil.lang.psi.MsilMethodEntry;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilMethodAsCSharpMethodDeclaration extends MsilMethodAsCSharpLikeMethodDeclaration implements CSharpMethodDeclaration {
    private static Map<String, MsiMappingOperator> ourOperatorNames = new HashMap<>() {
        {
            put("op_Addition", new MsiMappingOperator("+", CSharpTokens.PLUS));
            put("op_CheckedAddition", new MsiMappingOperator("+", CSharpTokens.PLUS, true));
            put("op_UnaryPlus", new MsiMappingOperator("+", CSharpTokens.PLUS));
            put("op_Subtraction", new MsiMappingOperator("-", CSharpTokens.MINUS));
            put("op_CheckedSubtraction", new MsiMappingOperator("-", CSharpTokens.MINUS, true));
            put("op_UnaryNegation", new MsiMappingOperator("-", CSharpTokens.MINUS));
            put("op_CheckedUnaryNegation", new MsiMappingOperator("-", CSharpTokens.MINUS, true));
            put("op_Multiply", new MsiMappingOperator("*", CSharpTokens.MUL));
            put("op_CheckedMultiply", new MsiMappingOperator("*", CSharpTokens.MUL, true));
            put("op_Division", new MsiMappingOperator("/", CSharpTokens.DIV));
            put("op_Modulus", new MsiMappingOperator("%", CSharpTokens.PERC));
            put("op_BitwiseAnd", new MsiMappingOperator("&", CSharpTokens.AND));
            put("op_BitwiseOr", new MsiMappingOperator("|", CSharpTokens.OR));
            put("op_ExclusiveOr", new MsiMappingOperator("^", CSharpTokens.XOR));
            put("op_LeftShift", new MsiMappingOperator("<<", CSharpTokens.LTLT));
            put("op_RightShift", new MsiMappingOperator(">>", CSharpTokens.GTGT));
            put("op_UnsignedRightShift", new MsiMappingOperator(">>", CSharpTokens.GTGT, true));
            put("op_Equality", new MsiMappingOperator("==", CSharpTokens.EQEQ));
            put("op_Inequality", new MsiMappingOperator("!=", CSharpTokens.NTEQ));
            put("op_LessThan", new MsiMappingOperator("<", CSharpTokens.LT));
            put("op_LessThanOrEqual", new MsiMappingOperator("<=", CSharpTokens.LTEQ));
            put("op_GreaterThan", new MsiMappingOperator(">", CSharpTokens.GT));
            put("op_GreaterThanOrEqual", new MsiMappingOperator(">=", CSharpTokens.GTEQ));
            put("op_OnesComplement", new MsiMappingOperator("~", CSharpTokens.TILDE));
            put("op_LogicalNot", new MsiMappingOperator("!", CSharpTokens.EXCL));
            put("op_Increment", new MsiMappingOperator("++", CSharpTokens.PLUSPLUS));
            put("op_CheckedIncrement", new MsiMappingOperator("++", CSharpTokens.PLUSPLUS, true));
            put("op_Decrement", new MsiMappingOperator("--", CSharpTokens.MINUSMINUS));
            put("op_CheckedDecrement", new MsiMappingOperator("--", CSharpTokens.MINUSMINUS, true));
        }
    };

    private final NullableLazyValue<String> myNameValue = new NullableLazyValue<>() {
        @Nullable
        @Override
        protected String compute() {
            MsiMappingOperator pair = ourOperatorNames.get(myOriginal.getName());
            if (pair != null) {
                return pair.tokenText();
            }
            return myDelegate == null ? MsilMethodAsCSharpMethodDeclaration.super.getName() : MsilHelper.cutGenericMarker(myDelegate.getName());
        }
    };

    private final NullableLazyValue<DotNetType> myTypeForImplementValue;

    private final MsilClassEntry myDelegate;

    @RequiredReadAction
    public MsilMethodAsCSharpMethodDeclaration(PsiElement parent, @Nullable MsilClassEntry declaration, @Nonnull GenericParameterContext genericParameterContext, @Nonnull MsilMethodEntry methodEntry) {
        super(parent, CSharpModifier.EMPTY_ARRAY, methodEntry);
        myDelegate = declaration;

        setGenericParameterList(declaration != null ? declaration : methodEntry, genericParameterContext);

        myTypeForImplementValue = NullableLazyValue.of(() ->
        {
            String nameFromBytecode = myOriginal.getNameFromBytecode();
            String typeBeforeDot = StringUtil.getPackageName(nameFromBytecode);
            SomeType someType = SomeTypeParser.parseType(typeBeforeDot, nameFromBytecode);
            if (someType != null) {
                return new DummyType(getProject(), MsilMethodAsCSharpMethodDeclaration.this, someType);
            }
            return null;
        });
    }

    @Override
    public void accept(@Nonnull CSharpElementVisitor visitor) {
        visitor.visitMethodDeclaration(this);
    }

    @RequiredReadAction
    @Override
    public String getName() {
        return myNameValue.getValue();
    }

    @RequiredReadAction
    @Nullable
    @Override
    public String getPresentableParentQName() {
        return myDelegate == null ? super.getPresentableParentQName() : myDelegate.getPresentableParentQName();
    }

    @RequiredReadAction
    @Nullable
    @Override
    public String getPresentableQName() {
        return myDelegate == null ? MsilHelper.append(getPresentableParentQName(), getName()) : MsilHelper.cutGenericMarker(myDelegate.getPresentableQName());
    }

    @Nullable
    @Override
    public CSharpGenericConstraintList getGenericConstraintList() {
        return myGenericConstraintListValue.getValue();
    }

    @Nonnull
    @Override
    public CSharpGenericConstraint[] getGenericConstraints() {
        CSharpGenericConstraintList genericConstraintList = getGenericConstraintList();
        return genericConstraintList == null ? CSharpGenericConstraint.EMPTY_ARRAY : genericConstraintList.getGenericConstraints();
    }

    @RequiredReadAction
    @Override
    public boolean isDelegate() {
        return myDelegate != null;
    }

    @RequiredReadAction
    @Override
    public boolean isOperator() {
        return ourOperatorNames.containsKey(myOriginal.getName());
    }

    @Override
    public boolean isExtension() {
        return CSharpMethodImplUtil.isExtensionMethod(this);
    }

    @RequiredReadAction
    @Nullable
    @Override
    public IElementType getOperatorElementType() {
        MsiMappingOperator pair = ourOperatorNames.get(myOriginal.getName());
        return pair == null ? null : pair.token();
    }

    @RequiredReadAction
    @Override
    public boolean isCheckedOperator() {
        MsiMappingOperator pair = ourOperatorNames.get(myOriginal.getName());
        return pair != null && pair.checked();
    }

    @RequiredReadAction
    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return null;
    }

    @Nullable
    @Override
    public DotNetType getTypeForImplement() {
        return myTypeForImplementValue.getValue();
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public DotNetTypeRef getTypeRefForImplement() {
        DotNetType typeForImplement = getTypeForImplement();
        return typeForImplement != null ? typeForImplement.toTypeRef() : DotNetTypeRef.ERROR_TYPE;
    }

    @Nullable
    @Override
    protected Class<? extends PsiElement> getNavigationElementClass() {
        return CSharpMethodDeclaration.class;
    }

    @Nullable
    public MsilClassEntry getDelegate() {
        return myDelegate;
    }
}
