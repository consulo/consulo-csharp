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

package org.mustbe.consulo.csharp.ide;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNativeTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNullableTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.DotNetPointerTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeWrapperWithTypeArgumentsImpl;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceType;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import lombok.val;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CSharpElementPresentationUtil extends DotNetElementPresentationUtil
{
	@NotNull
	public static String formatType(DotNetType type)
	{
		if(type == null)
		{
			return "<null>";
		}

		val builder = new StringBuilder();
		type.accept(new CSharpRecursiveElementVisitor()
		{
			@Override
			public void visitReferenceType(DotNetReferenceType type)
			{
				DotNetReferenceExpression referenceExpression = type.getReferenceExpression();
				if(referenceExpression == null)
				{
					builder.append("<null>");
				}
				else
				{
					builder.append(referenceExpression.getReferenceName());
				}
			}

			@Override
			public void visitTypeWrapperWithTypeArguments(CSharpTypeWrapperWithTypeArgumentsImpl typeArguments)
			{
				builder.append(formatType(typeArguments.getInnerType()));

				DotNetType[] arguments = typeArguments.getArguments();
				if(arguments.length > 0)
				{
					builder.append("<");
					for(int i = 0; i < arguments.length; i++)
					{
						DotNetType argument = arguments[i];
						if(i != 0)
						{
							builder.append(", ");
						}
						builder.append(formatType(argument));
					}
					builder.append(">");
				}
			}

			@Override
			public void visitNativeType(CSharpNativeTypeImpl type)
			{
				builder.append(type.getText());
			}

			@Override
			public void visitPointerType(DotNetPointerTypeImpl type)
			{
				builder.append(formatType(type.getInnerType()));
				builder.append("*");
			}

			@Override
			public void visitNullableType(CSharpNullableTypeImpl type)
			{
				builder.append(formatType(type.getInnerType()));
				builder.append("?");
			}

			@Override
			public void visitArrayType(CSharpArrayTypeImpl type)
			{
				builder.append(formatType(type.getInnerType()));
				builder.append("[]");
			}
		});
		return builder.toString();
	}
}
