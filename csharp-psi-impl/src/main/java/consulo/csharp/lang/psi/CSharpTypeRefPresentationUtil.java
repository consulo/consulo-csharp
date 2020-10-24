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

package consulo.csharp.lang.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.BitUtil;
import com.intellij.util.PairFunction;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.csharp.lang.psi.impl.source.resolve.type.*;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetPointerTypeRef;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.internal.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 03.09.14
 */
public class CSharpTypeRefPresentationUtil
{
	public static Map<String, String> ourTypesAsKeywords = new HashMap<String, String>()
	{
		{
			put(DotNetTypes.System.Object, "object");
			put(DotNetTypes.System.String, "string");
			put(DotNetTypes.System.SByte, "sbyte");
			put(DotNetTypes.System.Byte, "byte");
			put(DotNetTypes.System.Int16, "short");
			put(DotNetTypes.System.UInt16, "ushort");
			put(DotNetTypes.System.Int32, "int");
			put(DotNetTypes.System.UInt32, "uint");
			put(DotNetTypes.System.Int64, "long");
			put(DotNetTypes.System.UInt64, "ulong");
			put(DotNetTypes.System.Single, "float");
			put(DotNetTypes.System.Double, "double");
			put(DotNetTypes.System.Char, "char");
			put(DotNetTypes.System.Void, "void");
			put(DotNetTypes.System.Boolean, "bool");
			put(DotNetTypes.System.Decimal, "decimal");
		}
	};

	public static final int QUALIFIED_NAME = 1 << 0;
	public static final int TYPE_KEYWORD = 1 << 1;
	public static final int NO_GENERIC_ARGUMENTS = 1 << 2;
	public static final int NO_REF = 1 << 3;
	public static final int NULL = 1 << 4;
	public static final int NULLABLE = 1 << 5;

	public static final int QUALIFIED_NAME_WITH_KEYWORD = QUALIFIED_NAME | TYPE_KEYWORD;

	@Nonnull
	@RequiredReadAction
	public static String buildShortText(@Nonnull DotNetTypeRef typeRef)
	{
		StringBuilder builder = new StringBuilder();
		appendTypeRef(builder, typeRef, TYPE_KEYWORD | NULLABLE);
		return builder.toString();
	}

	@Nonnull
	@RequiredReadAction
	public static String buildText(@Nonnull DotNetTypeRef typeRef)
	{
		StringBuilder builder = new StringBuilder();
		appendTypeRef(builder, typeRef, QUALIFIED_NAME | NULLABLE);
		return builder.toString();
	}

	@Nonnull
	@RequiredReadAction
	public static String buildText(@Nonnull DotNetTypeRef typeRef, int flags)
	{
		StringBuilder builder = new StringBuilder();
		appendTypeRef(builder, typeRef, flags);
		return builder.toString();
	}

	@Nonnull
	@RequiredReadAction
	public static String buildTextWithKeyword(@Nonnull DotNetTypeRef typeRef)
	{
		StringBuilder builder = new StringBuilder();
		appendTypeRef(builder, typeRef, QUALIFIED_NAME | TYPE_KEYWORD | NULLABLE);
		return builder.toString();
	}

	@Nonnull
	@RequiredReadAction
	public static String buildTextWithKeywordAndNull(@Nonnull final DotNetTypeRef typeRef)
	{
		StringBuilder builder = new StringBuilder();
		appendTypeRef(builder, typeRef, QUALIFIED_NAME | TYPE_KEYWORD | NULL | NULLABLE);
		return builder.toString();
	}

	@RequiredReadAction
	public static void appendTypeRef(@Nonnull StringBuilder builder, @Nonnull DotNetTypeRef typeRef, final int flags)
	{
		Project project = typeRef.getProject();
		if(typeRef == DotNetTypeRef.AUTO_TYPE)
		{
			builder.append("var");
			return;
		}
		else if(typeRef instanceof CSharpNullTypeRef && BitUtil.isSet(flags, NULL))
		{
			builder.append("<null>");
			return;
		}

		if(typeRef instanceof CSharpStaticTypeRef || typeRef instanceof CSharpDynamicTypeRef)
		{
			builder.append(typeRef.toString());
		}
		else if(typeRef instanceof CSharpArrayTypeRef)
		{
			appendTypeRef(builder, ((CSharpArrayTypeRef) typeRef).getInnerTypeRef(), flags);
			builder.append("[");
			for(int i = 0; i < ((CSharpArrayTypeRef) typeRef).getDimensions(); i++)
			{
				builder.append(",");
			}
			builder.append("]");
		}
		else if(typeRef instanceof CSharpRefTypeRef)
		{
			if(!BitUtil.isSet(flags, NO_REF))
			{
				builder.append(((CSharpRefTypeRef) typeRef).getType().name());
				builder.append(" ");
			}
			appendTypeRef(builder, ((CSharpRefTypeRef) typeRef).getInnerTypeRef(), flags);
		}
		else if(typeRef instanceof CSharpEmptyGenericWrapperTypeRef)
		{
			appendTypeRef(builder, ((CSharpEmptyGenericWrapperTypeRef) typeRef).getInnerTypeRef(), flags | NO_GENERIC_ARGUMENTS);
			builder.append("<>");
		}
		else if(typeRef instanceof CSharpTupleTypeRef)
		{
			CSharpTupleTypeRef tupleTypeRef = (CSharpTupleTypeRef) typeRef;

			PsiNameIdentifierOwner[] variables = tupleTypeRef.getVariables();
			DotNetTypeRef[] typeRefs = tupleTypeRef.getTypeRefs();

			builder.append("(");
			for(int i = 0; i < variables.length; i++)
			{
				PsiNameIdentifierOwner tuplePartVar = variables[i];
				DotNetTypeRef tuplePartTypeRef = typeRefs[i];

				if(i != 0)
				{
					builder.append(", ");
				}

				appendTypeRef(builder, tuplePartTypeRef, flags);
				builder.append(" ");
				String name = tuplePartVar.getName();
				builder.append(name == null ? "Item" + (i + 1) : name);
			}
			builder.append(")");
		}
		else if(typeRef instanceof DotNetPointerTypeRef)
		{
			appendTypeRef(builder, ((DotNetPointerTypeRef) typeRef).getInnerTypeRef(), flags);
			builder.append("*");
		}
		else
		{
			DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
			DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();

			PsiElement element = typeResolveResult.getElement();

			if(BitUtil.isSet(flags, NULLABLE))
			{
				if(element instanceof DotNetTypeDeclaration)
				{
					String vmQName = ((DotNetTypeDeclaration) element).getVmQName();

					if(DotNetTypes.System.Nullable$1.equals(vmQName))
					{
						DotNetGenericParameter[] genericParameters = ((DotNetTypeDeclaration) element).getGenericParameters();

						if(genericParameters.length > 0)
						{
							DotNetGenericParameter firstParameter = genericParameters[0];

							DotNetTypeRef firstTypeRef = genericExtractor.extract(firstParameter);

							if(firstTypeRef != null)
							{
								appendTypeRef(builder, firstTypeRef, flags);
								builder.append("?");
								return;
							}
						}
					}
				}
			}

			if(element instanceof DotNetQualifiedElement)
			{
				String qName = ((DotNetQualifiedElement) element).getPresentableQName();
				String name = ((DotNetQualifiedElement) element).getName();

				String typeAsKeyword = CSharpCodeGenerationSettings.getInstance(project).USE_LANGUAGE_DATA_TYPES ? ourTypesAsKeywords.get(qName) : null;

				if(BitUtil.isSet(flags, QUALIFIED_NAME))
				{
					if(BitUtil.isSet(flags, TYPE_KEYWORD) && typeAsKeyword != null)
					{
						builder.append(typeAsKeyword);
					}
					else
					{
						builder.append(qName);
					}
				}
				else
				{
					if(BitUtil.isSet(flags, TYPE_KEYWORD) && typeAsKeyword != null)
					{
						builder.append(typeAsKeyword);
					}
					else
					{
						builder.append(name);
					}
				}
			}
			else
			{
				// fallback
				builder.append(typeRef.toString());
			}

			if(!BitUtil.isSet(flags, NO_GENERIC_ARGUMENTS))
			{
				if(element instanceof DotNetGenericParameterListOwner)
				{
					DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) element).getGenericParameters();
					if(genericParameters.length > 0)
					{
						builder.append("<");
						StubBlockUtil.join(builder, genericParameters, new PairFunction<StringBuilder, DotNetGenericParameter, Void>()
						{
							@Nullable
							@Override
							@RequiredReadAction
							public Void fun(StringBuilder t, DotNetGenericParameter v)
							{
								DotNetTypeRef extractedTypeRef = genericExtractor.extract(v);
								if(extractedTypeRef == null)
								{
									extractedTypeRef = new CSharpTypeRefFromGenericParameter(v);
								}
								appendTypeRef(t, extractedTypeRef, flags);
								return null;
							}
						}, ", ");
						builder.append(">");
					}
				}
			}
		}
	}
}
