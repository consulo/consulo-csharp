package org.mustbe.consulo.csharp.ide.msil.representation.builder;

import java.util.List;
import java.util.Map;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpEmptyGenericWrapperTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilCustomAttribute;
import org.mustbe.consulo.msil.lang.stubbing.MsilCustomAttributeArgumentList;
import org.mustbe.consulo.msil.lang.stubbing.MsilCustomAttributeStubber;
import org.mustbe.consulo.msil.lang.stubbing.values.MsiCustomAttributeValue;
import org.mustbe.consulo.msil.lang.stubbing.values.MsilCustomAttributeEnumValue;
import org.mustbe.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;
import org.mustbe.dotnet.msil.decompiler.textBuilder.util.XStubUtil;
import org.mustbe.dotnet.msil.decompiler.util.MsilHelper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.PairFunction;
import edu.arizona.cs.mbel.signature.ArrayShapeSignature;
import edu.arizona.cs.mbel.signature.ArrayTypeSignature;
import edu.arizona.cs.mbel.signature.ClassTypeSignature;
import edu.arizona.cs.mbel.signature.TypeSignature;
import edu.arizona.cs.mbel.signature.TypeSignatureWithGenericParameters;
import edu.arizona.cs.mbel.signature.ValueTypeSignature;

/**
 * @author VISTALL
 * @since 21.03.14
 */
@Logger
public class CSharpAttributeStubBuilder
{
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

	private static void appendValue(StringBuilder builder, MsiCustomAttributeValue value, final PsiElement scope)
	{
		if(value instanceof MsilCustomAttributeEnumValue)
		{
			final DotNetTypeRef typeRef = toTypeRef(value.getTypeSignature(), true);

			assert typeRef != null;

			List<String> values = ((MsilCustomAttributeEnumValue) value).getValues();

			StubBlockUtil.join(builder, values, new PairFunction<StringBuilder, String, Void>()
			{
				@Nullable
				@Override
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
			if(realValue instanceof String)
			{
				builder.append("\"");
				builder.append(XStubUtil.escapeChars((CharSequence) realValue));
				builder.append("\"");
			}
			else if(realValue instanceof TypeSignature)
			{
				DotNetTypeRef typeRef = toTypeRef((TypeSignature) realValue, true);
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
	private static DotNetTypeRef toTypeRef(TypeSignature typeSignature, boolean firstEnter)
	{
		if(typeSignature == null)
		{
			return null;
		}
		if(typeSignature == TypeSignature.BOOLEAN)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Boolean);
		}
		else if(typeSignature == TypeSignature.STRING)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.String);
		}
		else if(typeSignature == TypeSignature.U1)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Byte);
		}
		else if(typeSignature == TypeSignature.I1)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.SByte);
		}
		if(typeSignature == TypeSignature.U2)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.UInt16);
		}
		else if(typeSignature == TypeSignature.I2)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Int16);
		}
		else if(typeSignature == TypeSignature.U4)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.UInt32);
		}
		else if(typeSignature == TypeSignature.I4)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Int32);
		}
		else if(typeSignature == TypeSignature.U8)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.UInt64);
		}
		else if(typeSignature == TypeSignature.I8)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Int64);
		}
		else if(typeSignature == TypeSignature.R4)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Single);
		}
		else if(typeSignature == TypeSignature.R8)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Double);
		}
		else if(typeSignature == TypeSignature.CHAR)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Char);
		}
		else if(typeSignature instanceof TypeSignatureWithGenericParameters)
		{
			TypeSignatureWithGenericParameters typeSignatureWithGenericParameters =
					(TypeSignatureWithGenericParameters) typeSignature;
			List<TypeSignature> genericArguments = typeSignatureWithGenericParameters.getGenericArguments();
			DotNetTypeRef[] innerTypeRefs = new DotNetTypeRef[genericArguments.size()];
			for(int i = 0; i < innerTypeRefs.length; i++)
			{
				innerTypeRefs[i] = toTypeRef(genericArguments.get(i), false);
			}
			return new CSharpGenericWrapperTypeRef(toTypeRef(typeSignatureWithGenericParameters.getSignature(),
					false), innerTypeRefs);
		}
		else if(typeSignature instanceof ArrayTypeSignature)
		{
			ArrayShapeSignature arrayShape = ((ArrayTypeSignature) typeSignature).getArrayShape();
			return new CSharpArrayTypeRef(toTypeRef(((ArrayTypeSignature) typeSignature).getElementType(), false),
					arrayShape.getRank());
		}
		else if(typeSignature instanceof ValueTypeSignature)
		{
			return new CSharpTypeRefByQName(((ValueTypeSignature) typeSignature).getValueType().getFullName());
		}
		else if(typeSignature instanceof ClassTypeSignature)
		{
			String fullName = ((ClassTypeSignature) typeSignature).getClassType().getFullName();
			CSharpTypeRefByQName innerTypeRef = new CSharpTypeRefByQName(fullName);
			if(firstEnter)
			{
				if(StringUtil.containsChar(fullName, MsilHelper.GENERIC_MARKER_IN_NAME))
				{
					return new CSharpEmptyGenericWrapperTypeRef(innerTypeRef);
				}
			}
			return innerTypeRef;
		}
		else
		{
			LOGGER.error("Unknown how convert: " + typeSignature.toString() + ":0x" + Integer.toHexString
					(typeSignature.getType()));
			return new CSharpTypeRefByQName(DotNetTypes.System.Object);
		}
	}
}
