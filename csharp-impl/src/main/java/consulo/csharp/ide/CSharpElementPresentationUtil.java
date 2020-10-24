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

package consulo.csharp.ide;

import javax.annotation.Nonnull;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.BitUtil;
import com.intellij.util.Function;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.dotnet.psi.DotNetConstructorDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public class CSharpElementPresentationUtil
{
	public static final int SCALA_FORMAT = 1 << 0;
	public static final int METHOD_WITH_RETURN_TYPE = 1 << 1;
	public static final int METHOD_PARAMETER_NAME = 1 << 2;
	public static final int WITH_VIRTUAL_IMPL_TYPE = 1 << 4;
	public static final int NON_QUALIFIED_TYPE = 1 << 5;

	public static final int PROPERTY_SCALA_LIKE_FULL = SCALA_FORMAT | WITH_VIRTUAL_IMPL_TYPE;
	public static final int METHOD_SCALA_LIKE_FULL = SCALA_FORMAT | METHOD_WITH_RETURN_TYPE | METHOD_PARAMETER_NAME | WITH_VIRTUAL_IMPL_TYPE;

	@Nonnull
	@RequiredReadAction
	public static String formatField(@Nonnull DotNetFieldDeclaration fieldDeclaration)
	{
		return formatField(fieldDeclaration, 0);
	}

	@Nonnull
	@RequiredReadAction
	public static String formatField(@Nonnull DotNetFieldDeclaration fieldDeclaration, int flags)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(fieldDeclaration.getName());
		builder.append(":");
		CSharpTypeRefPresentationUtil.appendTypeRef(builder, fieldDeclaration.toTypeRef(true), typeRefMask(flags));
		return builder.toString();
	}

	@Nonnull
	@RequiredReadAction
	public static String formatProperty(@Nonnull DotNetPropertyDeclaration propertyDeclaration, int flags)
	{
		StringBuilder builder = new StringBuilder();

		if(BitUtil.isSet(flags, WITH_VIRTUAL_IMPL_TYPE))
		{
			DotNetTypeRef typeRefForImplement = propertyDeclaration.getTypeRefForImplement();
			if(typeRefForImplement != DotNetTypeRef.ERROR_TYPE)
			{
				CSharpTypeRefPresentationUtil.appendTypeRef(builder, typeRefForImplement, typeRefMask(flags));
				builder.append(".");
			}
		}

		if(BitUtil.isSet(flags, SCALA_FORMAT))
		{
			builder.append(propertyDeclaration.getName());
			builder.append(":");
			CSharpTypeRefPresentationUtil.appendTypeRef(builder, propertyDeclaration.toTypeRef(true), typeRefMask(flags));

		}
		else
		{
			CSharpTypeRefPresentationUtil.appendTypeRef(builder, propertyDeclaration.toTypeRef(true), typeRefMask(flags));

			builder.append(" ");
			builder.append(propertyDeclaration.getName());
		}
		return builder.toString();
	}

	@Nonnull
	@RequiredReadAction
	public static String formatMethod(@Nonnull DotNetLikeMethodDeclaration methodDeclaration, int flags)
	{
		StringBuilder builder = new StringBuilder();

		if(BitUtil.isSet(flags, METHOD_WITH_RETURN_TYPE) && !BitUtil.isSet(flags, SCALA_FORMAT))
		{
			if(!(methodDeclaration instanceof DotNetConstructorDeclaration))
			{
				CSharpTypeRefPresentationUtil.appendTypeRef(builder, methodDeclaration.getReturnTypeRef(), typeRefMask(flags));
				builder.append(" ");
			}
		}

		if(methodDeclaration instanceof DotNetConstructorDeclaration && ((DotNetConstructorDeclaration) methodDeclaration).isDeConstructor())
		{
			builder.append("~");
		}

		if(BitUtil.isSet(flags, WITH_VIRTUAL_IMPL_TYPE))
		{
			if(methodDeclaration instanceof DotNetVirtualImplementOwner)
			{
				DotNetTypeRef typeRefForImplement = ((DotNetVirtualImplementOwner) methodDeclaration).getTypeRefForImplement();
				if(typeRefForImplement != DotNetTypeRef.ERROR_TYPE)
				{
					CSharpTypeRefPresentationUtil.appendTypeRef(builder, typeRefForImplement, typeRefMask(flags));
					builder.append(".");
				}
			}
		}

		if(methodDeclaration instanceof CSharpIndexMethodDeclaration)
		{
			builder.append("this");
		}
		else
		{
			builder.append(methodDeclaration.getName());
		}
		formatTypeGenericParameters(methodDeclaration.getGenericParameters(), builder);
		formatParameters(methodDeclaration, builder, flags);
		return builder.toString();
	}

	@RequiredReadAction
	public static void formatParameters(@Nonnull DotNetLikeMethodDeclaration methodDeclaration, @Nonnull StringBuilder builder, final int flags)
	{
		boolean indexMethod = methodDeclaration instanceof CSharpIndexMethodDeclaration;

		DotNetParameter[] parameters = methodDeclaration.getParameters();
		if(parameters.length == 0)
		{
			builder.append(indexMethod ? "[]" : "()");
		}
		else
		{
			builder.append(indexMethod ? "[" : "(");
			builder.append(StringUtil.join(parameters, new Function<DotNetParameter, String>()
			{
				@Override
				@RequiredReadAction
				public String fun(DotNetParameter parameter)
				{
					String text = CSharpTypeRefPresentationUtil.buildTextWithKeyword(parameter.toTypeRef(true));

					if(!BitUtil.isSet(flags, METHOD_PARAMETER_NAME))
					{
						return text;
					}

					if(BitUtil.isSet(flags, SCALA_FORMAT))
					{
						return parameter.getName() + ":" + text;
					}
					else
					{
						return text + " " + parameter.getName();
					}
				}
			}, ", "));
			builder.append(indexMethod ? "]" : ")");
		}

		if(BitUtil.isSet(flags, METHOD_WITH_RETURN_TYPE) && BitUtil.isSet(flags, SCALA_FORMAT))
		{
			if(!(methodDeclaration instanceof DotNetConstructorDeclaration))
			{
				builder.append(":");
				CSharpTypeRefPresentationUtil.appendTypeRef(builder, methodDeclaration.getReturnTypeRef(), typeRefMask(flags));
			}
		}
	}

	private static int typeRefMask(int flags)
	{
		if(BitUtil.isSet(flags, NON_QUALIFIED_TYPE))
		{
			return CSharpTypeRefPresentationUtil.TYPE_KEYWORD;
		}
		return CSharpTypeRefPresentationUtil.QUALIFIED_NAME_WITH_KEYWORD;
	}

	public static void formatTypeGenericParameters(@Nonnull DotNetGenericParameter[] parameters, @Nonnull StringBuilder builder)
	{
		if(parameters.length > 0)
		{
			builder.append("<");
			builder.append(StringUtil.join(parameters, new Function<DotNetGenericParameter, String>()
			{
				@Override
				@RequiredReadAction
				public String fun(DotNetGenericParameter dotNetGenericParameter)
				{
					return dotNetGenericParameter.getName();
				}
			}, ", "));
			builder.append(">");
		}
	}

	@Nonnull
	@RequiredReadAction
	public static String formatGenericParameters(@Nonnull final DotNetGenericParameterListOwner owner, @Nonnull final DotNetGenericExtractor extractor)
	{
		DotNetGenericParameter[] genericParameters = owner.getGenericParameters();
		if(genericParameters.length == 0)
		{
			return "";
		}
		return "<" + StringUtil.join(genericParameters, new Function<DotNetGenericParameter, String>()
		{
			@Override
			@RequiredReadAction
			public String fun(DotNetGenericParameter genericParameter)
			{
				DotNetTypeRef extract = extractor.extract(genericParameter);
				if(extract != null)
				{
					return CSharpTypeRefPresentationUtil.buildShortText(extract);
				}
				return genericParameter.getName();
			}
		}, ", ") + ">";
	}
}
