package org.mustbe.consulo.csharp.ide.msil.representation.builder;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilCustomAttribute;
import org.mustbe.consulo.msil.lang.psi.MsilCustomAttributeSignature;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilArrayTypRefImpl;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilReferenceTypeRefImpl;
import org.mustbe.dotnet.msil.decompiler.textBuilder.util.XStubUtil;
import com.intellij.openapi.util.text.StringUtil;
import edu.arizona.cs.mbel.io.ByteBuffer;
import edu.arizona.cs.mbel.signature.ArrayTypeSignature;
import edu.arizona.cs.mbel.signature.ClassTypeSignature;
import edu.arizona.cs.mbel.signature.TypeSignature;
import edu.arizona.cs.mbel.signature.TypeSignatureParser;
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
		StringBuilder innerValue = null;

		MsilCustomAttributeSignature signature = attribute.getSignature();
		byte[] bytes = signature.getBytes();

		ByteBuffer byteBuffer = new ByteBuffer(bytes);

		if(byteBuffer.getShort() == 1)
		{
			innerValue = new StringBuilder();
			DotNetTypeRef[] parameterTypeRefs = attribute.getParameterList().getParameterTypeRefs();
			for(int i = 0; i < parameterTypeRefs.length; i++)
			{
				DotNetTypeRef parameterTypeRef = parameterTypeRefs[i];
				if(i != 0)
				{
					innerValue.append(", ");
				}
				try
				{
					appendValue(innerValue, parameterTypeRef, byteBuffer);
				}
				catch(Exception e)
				{
					innerValue = null;
					break;
				}
			}
		}

		if(innerValue != null && byteBuffer.canRead())
		{
			StringBuilder newBuilder = new StringBuilder();
			try
			{
				int count = byteBuffer.getShort();
				for(int i = 0; i < count; i++)
				{
					byteBuffer.get(); //type  0x53 field 0x54 property
					TypeSignature typeSignature = TypeSignatureParser.parse(byteBuffer, null);

					if(typeSignature == null)
					{
						continue;
					}
					newBuilder.append(", ");

					String name = XStubUtil.getUtf8(byteBuffer);

					newBuilder.append(name).append(" = ");

					appendValue(newBuilder, toTypeRef(typeSignature), byteBuffer);
				}
				innerValue.append(newBuilder);
			}
			catch(Exception e)
			{
				LOGGER.warn(e);
			}
		}

		if(StringUtil.isEmpty(innerValue))
		{
			return;
		}

		builder.append("(");
		builder.append(innerValue);
		builder.append(")");
	}

	@Nullable
	private static DotNetTypeRef toTypeRef(TypeSignature typeSignature)
	{
		if(typeSignature== null)
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
		else if(typeSignature instanceof ArrayTypeSignature)
		{
			return new CSharpArrayTypeRef(toTypeRef(((ArrayTypeSignature) typeSignature).getElementType()), 0);
		}
		else if(typeSignature instanceof ValueTypeSignature)
		{
			return new CSharpTypeRefByQName(((ValueTypeSignature) typeSignature).getValueType().getFullName());
		}
		else if(typeSignature instanceof ClassTypeSignature)
		{
			return new CSharpTypeRefByQName(((ClassTypeSignature) typeSignature).getClassType().getFullName());
		}
		else
		{
			LOGGER.error("Unknown how convert: " + typeSignature.toString() + ":0x" + Integer.toHexString(typeSignature.getType()));
			return new CSharpTypeRefByQName(DotNetTypes.System.Object);
		}
	}

	private static void appendValue(StringBuilder builder, DotNetTypeRef parameterTypeRef, ByteBuffer byteBuffer)
	{
		if(parameterTypeRef instanceof MsilArrayTypRefImpl)
		{
			builder.append("arrayError");
		}
		else if(parameterTypeRef instanceof MsilReferenceTypeRefImpl || parameterTypeRef instanceof CSharpTypeRefByQName)
		{
			String qualifiedText = parameterTypeRef.getQualifiedText();
			if(qualifiedText.equals(DotNetTypes.System.Int32))
			{
				builder.append(byteBuffer.getInt());
			}
			else if(qualifiedText.equals(DotNetTypes.System.UInt32))
			{
				builder.append(byteBuffer.getInt() & 0xFFFFFFFFL);
			}
			else if(qualifiedText.equals(DotNetTypes.System.Boolean))
			{
				builder.append(byteBuffer.get() == 1);
			}
			else if(qualifiedText.equals(DotNetTypes.System.Int16))
			{
				builder.append(byteBuffer.getShort());
			}
			else if(qualifiedText.equals(DotNetTypes.System.UInt16))
			{
				builder.append(byteBuffer.getShort() & 0xFFFF);
			}
			else if(qualifiedText.equals(DotNetTypes.System.Byte))
			{
				builder.append(byteBuffer.get() & 0xFF);
			}
			else if(qualifiedText.equals(DotNetTypes.System.SByte))
			{
				builder.append(byteBuffer.getShort());
			}
			else if(qualifiedText.equals(DotNetTypes.System.String))
			{
				builder.append(StringUtil.QUOTER.fun(XStubUtil.getUtf8(byteBuffer)));
			}
			else if(qualifiedText.equals(DotNetTypes.System.Type))
			{
				builder.append("typeof(System.Object");
				//FIXME [VISTALL] see #277 issue at consulo-csharp
				// builder.append(XStubUtil.getUtf8(byteBuffer));
				builder.append(")");
			}
			else
			{
				builder.append(StringUtil.QUOTER.fun("Unknown how build type: " + parameterTypeRef.getQualifiedText()));
			}
		}
		else
		{
			builder.append(StringUtil.QUOTER.fun("Unknown how build type: " + parameterTypeRef.getQualifiedText()));
		}
	}
}
