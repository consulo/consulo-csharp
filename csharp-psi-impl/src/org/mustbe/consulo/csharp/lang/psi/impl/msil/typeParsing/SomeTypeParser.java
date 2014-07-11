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

package org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromText;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.IntArrayList;

/**
 * @author VISTALL
 * @since 11.07.14
 */
public class SomeTypeParser
{
	@NotNull
	public static DotNetTypeRef toDotNetTypeRef(String text, PsiElement scope)
	{
		if(StringUtil.isEmpty(text))
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		SomeType someType = parseType(text);

		return convert(someType, scope);
	}

	private static DotNetTypeRef convert(SomeType type, final PsiElement scope)
	{
		final Ref<DotNetTypeRef> typeRefRef = new Ref<DotNetTypeRef>();
		type.accept(new SomeTypeVisitor()
		{
			@Override
			public void visitUserType(UserType userType)
			{
				typeRefRef.set(new CSharpTypeRefFromText(userType.getText(), scope));
			}

			@Override
			public void visitGenericWrapperType(GenericWrapperType genericWrapperType)
			{
				List<SomeType> arguments = genericWrapperType.getArguments();
				List<DotNetTypeRef> typeRefs = new ArrayList<DotNetTypeRef>(arguments.size());
				for(SomeType argument : arguments)
				{
					typeRefs.add(convert(argument, scope));
				}

				typeRefRef.set(new DotNetGenericWrapperTypeRef(convert(genericWrapperType.getTarget(), scope),
						typeRefs.toArray(new DotNetTypeRef[typeRefs.size()])));
			}
		});
		return typeRefRef.get();
	}

	public static SomeType parseType(String text)
	{
		int i = text.indexOf("<");
		if(i != -1)
		{
			String innerType = text.substring(0, i);

			String argumentsList = text.substring(i + 1, text.lastIndexOf(">"));

			List<String> types = splitButIgnoreInsideLtGt(argumentsList);

			List<SomeType> map = ContainerUtil.map(types, new Function<String, SomeType>()
			{
				@Override
				public SomeType fun(String s)
				{
					return parseType(s);
				}
			});
			return new GenericWrapperType(new UserType(innerType), map);
		}
		else
		{
			return new UserType(text);
		}
	}

	private static List<String> splitButIgnoreInsideLtGt(String text)
	{
		IntArrayList list = new IntArrayList();

		int dep = 0;

		char[] chars = text.toCharArray();
		for(int i = 0; i < chars.length; i++)
		{
			switch(chars[i])
			{
				case '<':
					dep++;
					break;
				case '>':
					dep--;
					break;
				case ',':
					if(dep == 0)
					{
						list.add(i);
					}
					break;
			}
		}

		list.add(text.length());

		List<String> split = new ArrayList<String>(list.size());
		int last = 0;
		for(int i : list.toArray())
		{
			String substring = text.substring(last, i);
			split.add(substring);
			last = i + 1;
		}
		return split;
	}
}
