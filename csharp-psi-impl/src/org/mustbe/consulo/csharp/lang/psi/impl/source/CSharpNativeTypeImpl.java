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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetNativeType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 13.12.13.
 */
public class CSharpNativeTypeImpl extends CSharpElementImpl implements DotNetNativeType
{
	public static final Map<IElementType, CSharpNativeTypeRef> ELEMENT_TYPE_TO_TYPE = new HashMap<IElementType, CSharpNativeTypeRef>()
	{
		{
			put(CSharpTokens.BOOL_KEYWORD, CSharpNativeTypeRef.BOOL);
			put(CSharpTokens.DOUBLE_KEYWORD, CSharpNativeTypeRef.DOUBLE);
			put(CSharpTokens.FLOAT_KEYWORD, CSharpNativeTypeRef.FLOAT);
			put(CSharpTokens.CHAR_KEYWORD, CSharpNativeTypeRef.CHAR);
			put(CSharpTokens.OBJECT_KEYWORD, CSharpNativeTypeRef.OBJECT);
			put(CSharpTokens.STRING_KEYWORD, CSharpNativeTypeRef.STRING);
			put(CSharpTokens.SBYTE_KEYWORD, CSharpNativeTypeRef.SBYTE);
			put(CSharpTokens.BYTE_KEYWORD, CSharpNativeTypeRef.BYTE);
			put(CSharpTokens.INT_KEYWORD, CSharpNativeTypeRef.INT);
			put(CSharpTokens.UINT_KEYWORD, CSharpNativeTypeRef.UINT);
			put(CSharpTokens.LONG_KEYWORD, CSharpNativeTypeRef.LONG);
			put(CSharpTokens.ULONG_KEYWORD, CSharpNativeTypeRef.ULONG);
			put(CSharpTokens.VOID_KEYWORD, CSharpNativeTypeRef.VOID);
			put(CSharpTokens.SHORT_KEYWORD, CSharpNativeTypeRef.SHORT);
			put(CSharpTokens.USHORT_KEYWORD, CSharpNativeTypeRef.USHORT);
			put(CSharpTokens.DECIMAL_KEYWORD, CSharpNativeTypeRef.DECIMAL);
			put(CSharpTokens.IMPLICIT_KEYWORD, CSharpNativeTypeRef.IMPLICIT);
			put(CSharpTokens.EXPLICIT_KEYWORD, CSharpNativeTypeRef.EXPLICIT);
			put(CSharpTokens.DYNAMIC_KEYWORD, CSharpNativeTypeRef.DYNAMIC);
		}
	};

	public CSharpNativeTypeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitNativeType(this);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		IElementType elementType = getTypeElement().getNode().getElementType();
		if(elementType == CSharpSoftTokens.VAR_KEYWORD)
		{
			return DotNetTypeRef.AUTO_TYPE;
		}
		CSharpNativeTypeRef cSharpNativeTypeRef = ELEMENT_TYPE_TO_TYPE.get(elementType);
		assert cSharpNativeTypeRef != null : elementType.toString();
		return cSharpNativeTypeRef;
	}

	@NotNull
	@Override
	public PsiElement getTypeElement()
	{
		return findNotNullChildByFilter(CSharpTokenSets.NATIVE_TYPES);
	}
}
