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

package consulo.csharp.lang.impl.psi.msil.typeParsing;

import consulo.component.ProcessCanceledException;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefFromText;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.primitive.ints.IntList;
import consulo.util.collection.primitive.ints.IntLists;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.Ref;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 11.07.14
 */
public class SomeTypeParser
{
	private static final Logger LOGGER = Logger.getInstance(SomeTypeParser.class);

	@Nonnull
	public static DotNetTypeRef toDotNetTypeRef(String text, String nameFromBytecode, PsiElement scope)
	{
		SomeType someType = parseType(text, nameFromBytecode);

		if(someType == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return convert(someType, scope);
	}

	public static DotNetTypeRef convert(@Nonnull SomeType type, @Nonnull final PsiElement scope)
	{
		final Ref<DotNetTypeRef> typeRefRef = new Ref<>();
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
				List<DotNetTypeRef> typeRefs = new ArrayList<>(arguments.size());
				for(SomeType argument : arguments)
				{
					typeRefs.add(convert(argument, scope));
				}

				Project project = scope.getProject();
				GlobalSearchScope resolveScope = scope.getResolveScope();
				typeRefRef.set(new CSharpGenericWrapperTypeRef(project, resolveScope, convert(genericWrapperType.getTarget(), scope), typeRefs.toArray(new DotNetTypeRef[typeRefs.size()])));
			}
		});
		return typeRefRef.get();
	}

	@Nullable
	public static SomeType parseType(String text, final String nameFromBytecode)
	{
		if(StringUtil.isEmpty(text))
		{
			return null;
		}

		try
		{
			int i = text.indexOf("<");
			if(i != -1)
			{
				String innerType = text.substring(0, i);

				String argumentsList = text.substring(i + 1, text.lastIndexOf(">"));

				List<String> types = splitButIgnoreInsideLtGt(argumentsList);

				List<SomeType> map = ContainerUtil.map(types, s -> parseType(s, nameFromBytecode));
				return new GenericWrapperType(new UserType(innerType), map);
			}
			else
			{
				return new UserType(text);
			}
		}
		catch(ProcessCanceledException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			SomeTypeParser.LOGGER.error("Type " + nameFromBytecode + " cant parsed");
			return new UserType(DotNetTypes.System.Object);
		}
	}

	private static List<String> splitButIgnoreInsideLtGt(String text)
	{
		IntList list = IntLists.newArrayList();

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

		List<String> split = new ArrayList<>(list.size());
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
