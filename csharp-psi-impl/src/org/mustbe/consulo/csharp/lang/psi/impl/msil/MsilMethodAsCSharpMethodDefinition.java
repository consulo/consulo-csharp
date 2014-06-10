/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.msil.MsilHelper;
import org.mustbe.consulo.msil.lang.psi.MsilClassEntry;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilMethodAsCSharpMethodDefinition extends MsilMethodAsCSharpLikeMethodDeclaration implements CSharpMethodDeclaration
{
	private static Map<String, Pair<String, IElementType>> ourOperatorNames = new HashMap<String, Pair<String, IElementType>>()
	{
		{
			put("op_Addition", new Pair<String, IElementType>("+", CSharpTokens.PLUS));
			put("op_UnaryPlus", new Pair<String, IElementType>("+", CSharpTokens.PLUS));
			put("op_Subtraction", new Pair<String, IElementType>("-", CSharpTokens.MINUS));
			put("op_UnaryNegation", new Pair<String, IElementType>("-", CSharpTokens.MINUS));
			put("op_Multiply", new Pair<String, IElementType>("*", CSharpTokens.MUL));
			put("op_Division", new Pair<String, IElementType>("/", CSharpTokens.DIV));
			put("op_Modulus", new Pair<String, IElementType>("%", CSharpTokens.PERC));
			put("op_BitwiseAnd", new Pair<String, IElementType>("&", CSharpTokens.AND));
			put("op_BitwiseOr", new Pair<String, IElementType>("|", CSharpTokens.OR));
			put("op_ExclusiveOr", new Pair<String, IElementType>("^", CSharpTokens.XOR));
			put("op_LeftShift", new Pair<String, IElementType>("<<", CSharpTokens.LTLT));
			put("op_RightShift", new Pair<String, IElementType>(">>", CSharpTokens.GTGT));
			put("op_Equality", new Pair<String, IElementType>("==", CSharpTokens.EQEQ));
			put("op_Inequality", new Pair<String, IElementType>("!=", CSharpTokens.NTEQ));
			put("op_LessThan", new Pair<String, IElementType>("<", CSharpTokens.LT));
			put("op_LessThanOrEqual", new Pair<String, IElementType>("<=", CSharpTokens.LTEQ));
			put("op_GreaterThan", new Pair<String, IElementType>(">", CSharpTokens.GT));
			put("op_GreaterThanOrEqual", new Pair<String, IElementType>(">=", CSharpTokens.GTEQ));
			put("op_OnesComplement", new Pair<String, IElementType>("~", CSharpTokens.TILDE));
			put("op_LogicalNot", new Pair<String, IElementType>("!", CSharpTokens.EXCL));
			put("op_Increment", new Pair<String, IElementType>("++", CSharpTokens.PLUSPLUS));
			put("op_Decrement", new Pair<String, IElementType>("--", CSharpTokens.MINUSMINUS));
			//put("op_Explicit", "implicit");
			//put("op_Implicit", "explicit");
		}
	};

	private final MsilClassEntry myDelegate;

	public MsilMethodAsCSharpMethodDefinition(PsiElement parent, @Nullable MsilClassEntry msilClassEntry, MsilMethodEntry methodEntry)
	{
		super(parent, methodEntry);
		myDelegate = msilClassEntry;
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpElementVisitor)
		{
			((CSharpElementVisitor) visitor).visitMethodDeclaration(this);
		}
		else
		{
			visitor.visitElement(this);
		}
	}

	@Override
	public String getName()
	{
		Pair<String, IElementType> pair = ourOperatorNames.get(myMsilElement.getName());
		if(pair != null)
		{
			return pair.getFirst();
		}
		return myDelegate == null ? super.getName() : MsilHelper.cutGenericMarker(myDelegate.getName());
	}

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myDelegate == null ? super.getPresentableParentQName() : myDelegate.getPresentableParentQName();
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myDelegate == null ? MsilHelper.append(getPresentableParentQName(), getName()) : MsilHelper.cutGenericMarker(myDelegate
				.getPresentableQName());
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return null;
	}

	@NotNull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return new CSharpGenericConstraint[0];
	}

	@Override
	public boolean isDelegate()
	{
		return myDelegate != null;
	}

	@Override
	public boolean isOperator()
	{
		return ourOperatorNames.containsKey(myMsilElement.getName());
	}

	@Nullable
	@Override
	public IElementType getOperatorElementType()
	{
		Pair<String, IElementType> pair = ourOperatorNames.get(myMsilElement.getName());
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

	@NotNull
	public String getNameFromBytecode()
	{
		return myMsilElement.getNameFromBytecode();
	}
}
