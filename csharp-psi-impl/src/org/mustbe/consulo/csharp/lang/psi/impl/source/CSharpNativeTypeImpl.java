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
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
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
	public static final Map<IElementType, String> ourElementToQTypes = new HashMap<IElementType, String>()
	{
		{
			put(CSharpTokens.BOOL_KEYWORD, DotNetTypes.System.Boolean);
			put(CSharpTokens.DOUBLE_KEYWORD, DotNetTypes.System.Double);
			put(CSharpTokens.FLOAT_KEYWORD, DotNetTypes.System.Single);
			put(CSharpTokens.CHAR_KEYWORD, DotNetTypes.System.Char);
			put(CSharpTokens.OBJECT_KEYWORD, DotNetTypes.System.Object);
			put(CSharpTokens.STRING_KEYWORD, DotNetTypes.System.String);
			put(CSharpTokens.SBYTE_KEYWORD, DotNetTypes.System.SByte);
			put(CSharpTokens.BYTE_KEYWORD, DotNetTypes.System.Byte);
			put(CSharpTokens.INT_KEYWORD, DotNetTypes.System.Int32);
			put(CSharpTokens.UINT_KEYWORD, DotNetTypes.System.UInt32);
			put(CSharpTokens.LONG_KEYWORD, DotNetTypes.System.Int64);
			put(CSharpTokens.ULONG_KEYWORD, DotNetTypes.System.UInt64);
			put(CSharpTokens.VOID_KEYWORD, DotNetTypes.System.Void);
			put(CSharpTokens.SHORT_KEYWORD, DotNetTypes.System.Int16);
			put(CSharpTokens.USHORT_KEYWORD, DotNetTypes.System.UInt16);
			put(CSharpTokens.DECIMAL_KEYWORD, DotNetTypes.System.Decimal);
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
		else if(elementType == CSharpTokens.IMPLICIT_KEYWORD)
		{
			return CSharpStaticTypeRef.IMPLICIT;
		}
		else if(elementType == CSharpTokens.EXPLICIT_KEYWORD)
		{
			return CSharpStaticTypeRef.EXPLICIT;
		}
		else if(elementType == CSharpTokens.DYNAMIC_KEYWORD)
		{
			return CSharpStaticTypeRef.DYNAMIC;
		}

		String q = ourElementToQTypes.get(elementType);
		assert q != null : elementType.toString();
		boolean nullable = false;
		if(DotNetTypes.System.Object.equals(q) || DotNetTypes.System.String.equals(q))
		{
			nullable = true;
		}
		return new DotNetTypeRefByQName(q, CSharpTransform.INSTANCE, nullable);
	}

	@NotNull
	@Override
	public PsiElement getTypeElement()
	{
		return findNotNullChildByFilter(CSharpTokenSets.NATIVE_TYPES);
	}
}
