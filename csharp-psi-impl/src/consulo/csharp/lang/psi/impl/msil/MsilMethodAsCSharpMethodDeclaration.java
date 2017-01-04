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

package consulo.csharp.lang.psi.impl.msil;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.msil.typeParsing.SomeType;
import consulo.csharp.lang.psi.impl.msil.typeParsing.SomeTypeParser;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;
import consulo.lombok.annotations.Lazy;
import consulo.msil.lang.psi.MsilClassEntry;
import consulo.msil.lang.psi.MsilMethodEntry;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilMethodAsCSharpMethodDeclaration extends MsilMethodAsCSharpLikeMethodDeclaration implements CSharpMethodDeclaration
{
	private static Map<String, Pair<String, IElementType>> ourOperatorNames = new HashMap<String, Pair<String, IElementType>>()
	{
		{
			put("op_Addition", Pair.create("+", CSharpTokens.PLUS));
			put("op_UnaryPlus", Pair.create("+", CSharpTokens.PLUS));
			put("op_Subtraction", Pair.create("-", CSharpTokens.MINUS));
			put("op_UnaryNegation", Pair.create("-", CSharpTokens.MINUS));
			put("op_Multiply", Pair.create("*", CSharpTokens.MUL));
			put("op_Division", Pair.create("/", CSharpTokens.DIV));
			put("op_Modulus", Pair.create("%", CSharpTokens.PERC));
			put("op_BitwiseAnd", Pair.create("&", CSharpTokens.AND));
			put("op_BitwiseOr", Pair.create("|", CSharpTokens.OR));
			put("op_ExclusiveOr", Pair.create("^", CSharpTokens.XOR));
			put("op_LeftShift", Pair.create("<<", CSharpTokens.LTLT));
			put("op_RightShift", Pair.create(">>", CSharpTokens.GTGT));
			put("op_Equality", Pair.create("==", CSharpTokens.EQEQ));
			put("op_Inequality", Pair.create("!=", CSharpTokens.NTEQ));
			put("op_LessThan", Pair.create("<", CSharpTokens.LT));
			put("op_LessThanOrEqual", Pair.create("<=", CSharpTokens.LTEQ));
			put("op_GreaterThan", Pair.create(">", CSharpTokens.GT));
			put("op_GreaterThanOrEqual", Pair.create(">=", CSharpTokens.GTEQ));
			put("op_OnesComplement", Pair.create("~", CSharpTokens.TILDE));
			put("op_LogicalNot", Pair.create("!", CSharpTokens.EXCL));
			put("op_Increment", Pair.create("++", CSharpTokens.PLUSPLUS));
			put("op_Decrement", Pair.create("--", CSharpTokens.MINUSMINUS));
		}
	};

	private NullableLazyValue<String> myNameValue = new NullableLazyValue<String>()
	{
		@Nullable
		@Override
		protected String compute()
		{
			Pair<String, IElementType> pair = ourOperatorNames.get(myOriginal.getName());
			if(pair != null)
			{
				return pair.getFirst();
			}
			return myDelegate == null ? MsilMethodAsCSharpMethodDeclaration.super.getName() : MsilHelper.cutGenericMarker(myDelegate.getName());
		}
	};

	private final MsilClassEntry myDelegate;

	public MsilMethodAsCSharpMethodDeclaration(PsiElement parent, @Nullable MsilClassEntry declaration, @NotNull GenericParameterContext genericParameterContext, @NotNull MsilMethodEntry methodEntry)
	{
		super(parent, CSharpModifier.EMPTY_ARRAY, methodEntry);
		myDelegate = declaration;

		setGenericParameterList(declaration != null ? declaration : methodEntry, genericParameterContext);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitMethodDeclaration(this);
	}

	@Override
	public String getName()
	{
		return myNameValue.getValue();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myDelegate == null ? super.getPresentableParentQName() : myDelegate.getPresentableParentQName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myDelegate == null ? MsilHelper.append(getPresentableParentQName(), getName()) : MsilHelper.cutGenericMarker(myDelegate.getPresentableQName());
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return myGenericConstraintListValue.getValue();
	}

	@NotNull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		CSharpGenericConstraintList genericConstraintList = getGenericConstraintList();
		return genericConstraintList == null ? CSharpGenericConstraint.EMPTY_ARRAY : genericConstraintList.getGenericConstraints();
	}

	@Override
	public boolean isDelegate()
	{
		return myDelegate != null;
	}

	@Override
	public boolean isOperator()
	{
		return ourOperatorNames.containsKey(myOriginal.getName());
	}

	@Override
	public boolean isExtension()
	{
		return CSharpMethodImplUtil.isExtensionMethod(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public IElementType getOperatorElementType()
	{
		Pair<String, IElementType> pair = ourOperatorNames.get(myOriginal.getName());
		if(pair == null)
		{
			return null;
		}
		return pair.getSecond();
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Nullable
	@Override
	@Lazy(notNull = false)
	public DotNetType getTypeForImplement()
	{
		String nameFromBytecode = myOriginal.getNameFromBytecode();
		String typeBeforeDot = StringUtil.getPackageName(nameFromBytecode);
		SomeType someType = SomeTypeParser.parseType(typeBeforeDot, nameFromBytecode);
		if(someType != null)
		{
			return new DummyType(getProject(), MsilMethodAsCSharpMethodDeclaration.this, someType);
		}
		return null;
	}

	@NotNull
	@Override
	@Lazy
	public DotNetTypeRef getTypeRefForImplement()
	{
		DotNetType typeForImplement = getTypeForImplement();
		return typeForImplement != null ? typeForImplement.toTypeRef() : DotNetTypeRef.ERROR_TYPE;
	}

	@Nullable
	@Override
	protected Class<? extends PsiElement> getNavigationElementClass()
	{
		return CSharpMethodDeclaration.class;
	}

	@Nullable
	public MsilClassEntry getDelegate()
	{
		return myDelegate;
	}
}
