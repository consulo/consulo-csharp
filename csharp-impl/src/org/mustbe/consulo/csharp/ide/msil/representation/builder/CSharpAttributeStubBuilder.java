package org.mustbe.consulo.csharp.ide.msil.representation.builder;

import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilCustomAttribute;
import org.mustbe.consulo.msil.lang.psi.MsilCustomAttributeSignature;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilArrayTypRefImpl;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilReferenceTypeRefImpl;
import org.mustbe.dotnet.msil.decompiler.textBuilder.util.XStubUtil;
import com.intellij.openapi.util.text.StringUtil;
import edu.arizona.cs.mbel.io.ByteBuffer;

/**
 * @author VISTALL
 * @since 21.03.14
 */
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
			for(DotNetTypeRef parameterTypeRef : parameterTypeRefs)
			{
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

		builder.append("(");
		if(innerValue == null)
		{
			builder.append(StringUtil.QUOTER.fun("ERRROR"));
		}
		else
		{
			builder.append(innerValue);
		}
		builder.append(")");
	}

	private static void appendValue(StringBuilder builder, DotNetTypeRef parameterTypeRef, ByteBuffer byteBuffer)
	{
		if(parameterTypeRef instanceof MsilArrayTypRefImpl)
		{
			builder.append("arrayError");
		}
		else if(parameterTypeRef instanceof MsilReferenceTypeRefImpl)
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
