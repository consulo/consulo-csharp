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

package consulo.csharp.ide.msil.representation.builder;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.PairFunction;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpEmptyGenericWrapperTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.internal.dotnet.asm.signature.ArrayShapeSignature;
import consulo.internal.dotnet.asm.signature.ArrayTypeSignature;
import consulo.internal.dotnet.asm.signature.ClassTypeSignature;
import consulo.internal.dotnet.asm.signature.TypeSignature;
import consulo.internal.dotnet.asm.signature.TypeSignatureWithGenericParameters;
import consulo.internal.dotnet.asm.signature.ValueTypeSignature;
import consulo.internal.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;
import consulo.msil.lang.psi.MsilCustomAttribute;
import consulo.msil.lang.stubbing.MsilCustomAttributeArgumentList;
import consulo.msil.lang.stubbing.MsilCustomAttributeStubber;
import consulo.msil.lang.stubbing.values.MsiCustomAttributeValue;
import consulo.msil.lang.stubbing.values.MsilCustomAttributeEnumValue;

/**
 * @author VISTALL
 * @since 21.03.14
 */
public class CSharpAttributeStubBuilder
{
	private static final Logger LOGGER = Logger.getInstance(CSharpAttributeStubBuilder.class);

	@RequiredReadAction
	public static void append(StringBuilder builder, MsilCustomAttribute attribute)
	{
		MsilCustomAttributeArgumentList attributeArgumentList = MsilCustomAttributeStubber.build(attribute);

		List<MsiCustomAttributeValue> constructorArguments = attributeArgumentList.getConstructorArguments();
		Map<String, MsiCustomAttributeValue> namedArguments = attributeArgumentList.getNamedArguments();
		if(constructorArguments.isEmpty() && namedArguments.isEmpty())
		{
			return;
		}

		builder.append("(");
		for(int i = 0; i < constructorArguments.size(); i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}

			appendValue(builder, constructorArguments.get(i), attribute);
		}

		if(!constructorArguments.isEmpty() && !namedArguments.isEmpty())
		{
			builder.append(", ");
		}

		boolean first = false;
		for(Map.Entry<String, MsiCustomAttributeValue> entry : namedArguments.entrySet())
		{
			if(!first)
			{
				first = true;
			}
			else
			{
				builder.append(", ");
			}

			builder.append(entry.getKey()).append(" = ");
			appendValue(builder, entry.getValue(), attribute);
		}
		builder.append(")");
	}

	@RequiredReadAction
	private static void appendValue(StringBuilder builder, MsiCustomAttributeValue value, final PsiElement scope)
	{
		if(value instanceof MsilCustomAttributeEnumValue)
		{
			final DotNetTypeRef typeRef = toTypeRef(scope, value.getTypeSignature(), true);

			assert typeRef != null;

			List<String> values = ((MsilCustomAttributeEnumValue) value).getValues();

			StubBlockUtil.join(builder, values, new PairFunction<StringBuilder, String, Void>()
			{
				@Nullable
				@Override
				@RequiredReadAction
				public Void fun(StringBuilder builder, String s)
				{
					CSharpStubBuilderVisitor.appendTypeRef(scope, builder, typeRef);
					builder.append(".").append(s);
					return null;
				}
			}, " | ");
		}
		else
		{
			Object realValue = value.getValue();
			if(realValue instanceof CharSequence)
			{
				builder.append("\"");
				builder.append((CharSequence) realValue);
				builder.append("\"");
			}
			else if(realValue instanceof TypeSignature)
			{
				DotNetTypeRef typeRef = toTypeRef(scope, (TypeSignature) realValue, true);
				assert typeRef != null;
				builder.append("typeof(");
				CSharpStubBuilderVisitor.appendTypeRef(scope, builder, typeRef);
				builder.append(")");
			}
			else
			{
				builder.append(realValue);
			}
		}
	}

	@Nullable
	@RequiredReadAction
	private static DotNetTypeRef toTypeRef(PsiElement scope, TypeSignature typeSignature, boolean firstEnter)
	{
		if(typeSignature == null)
		{
			return null;
		}
		if(typeSignature == TypeSignature.BOOLEAN)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.Boolean);
		}
		else if(typeSignature == TypeSignature.STRING)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.String);
		}
		else if(typeSignature == TypeSignature.U1)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.Byte);
		}
		else if(typeSignature == TypeSignature.I1)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.SByte);
		}
		if(typeSignature == TypeSignature.U2)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.UInt16);
		}
		else if(typeSignature == TypeSignature.I2)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.Int16);
		}
		else if(typeSignature == TypeSignature.U4)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.UInt32);
		}
		else if(typeSignature == TypeSignature.I4)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.Int32);
		}
		else if(typeSignature == TypeSignature.U8)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.UInt64);
		}
		else if(typeSignature == TypeSignature.I8)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.Int64);
		}
		else if(typeSignature == TypeSignature.R4)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.Single);
		}
		else if(typeSignature == TypeSignature.R8)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.Double);
		}
		else if(typeSignature == TypeSignature.CHAR)
		{
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.Char);
		}
		else if(typeSignature instanceof TypeSignatureWithGenericParameters)
		{
			TypeSignatureWithGenericParameters typeSignatureWithGenericParameters = (TypeSignatureWithGenericParameters) typeSignature;
			List<TypeSignature> genericArguments = typeSignatureWithGenericParameters.getGenericArguments();
			DotNetTypeRef[] innerTypeRefs = new DotNetTypeRef[genericArguments.size()];
			for(int i = 0; i < innerTypeRefs.length; i++)
			{
				innerTypeRefs[i] = toTypeRef(scope, genericArguments.get(i), false);
			}
			return new CSharpGenericWrapperTypeRef(scope.getProject(), toTypeRef(scope, typeSignatureWithGenericParameters.getSignature(), false), innerTypeRefs);
		}
		else if(typeSignature instanceof ArrayTypeSignature)
		{
			ArrayShapeSignature arrayShape = ((ArrayTypeSignature) typeSignature).getArrayShape();
			return new CSharpArrayTypeRef(scope, toTypeRef(scope, ((ArrayTypeSignature) typeSignature).getElementType(), false), arrayShape.getRank());
		}
		else if(typeSignature instanceof ValueTypeSignature)
		{
			return new CSharpTypeRefByQName(scope, ((ValueTypeSignature) typeSignature).getValueType().getFullName());
		}
		else if(typeSignature instanceof ClassTypeSignature)
		{
			String fullName = ((ClassTypeSignature) typeSignature).getClassType().getFullName();
			CSharpTypeRefByQName innerTypeRef = new CSharpTypeRefByQName(scope, fullName);
			if(firstEnter)
			{
				if(StringUtil.containsChar(fullName, MsilHelper.GENERIC_MARKER_IN_NAME))
				{
					return new CSharpEmptyGenericWrapperTypeRef(scope.getProject(), innerTypeRef);
				}
			}
			return innerTypeRef;
		}
		else
		{
			LOGGER.error("Unknown how convert: " + typeSignature.toString() + ":0x" + Integer.toHexString(typeSignature.getType()));
			return new CSharpTypeRefByQName(scope, DotNetTypes.System.Object);
		}
	}
}
