/*
 * Copyright 2013-2026 consulo.io
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

package consulo.csharp.it.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.ReadAction;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The overload resolution tests of Roslyn which state the method each call binds to in a {@code //-} comment after the
 * call ({@code TestOverloadResolutionWithDiff}), extracted by {@code roslyn/overloadResolution/extract.py}: every call in
 * {@code C.M} must resolve to that method, in order.
 * <p>
 * Generic arguments of the owner and of the method are left out of the comparison - the resolved method keeps no
 * inferred extractor - but inference still shows in the substituted parameter types.
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class RoslynOverloadResolutionTest
{
	private static final int TYPE_FLAGS = CSharpTypeRefPresentationUtil.QUALIFIED_NAME | CSharpTypeRefPresentationUtil.FORCE_TYPE_KEYWORD;

	static Stream<String> sources() throws Exception
	{
		try(Stream<Path> files = Files.list(CSharpResolveTesting.resource("roslyn/overloadResolution")))
		{
			return files.map(path -> path.getFileName().toString()).filter(name -> name.endsWith(".cs")).sorted().toList().stream();
		}
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("sources")
	public void overloadResolution(String fileName, HeadlessProjects projects) throws Exception
	{
		String source = Files.readString(CSharpResolveTesting.resource("roslyn/overloadResolution/" + fileName), StandardCharsets.UTF_8);

		List<String> expected = new ArrayList<>();
		for(String line : source.split("\n"))
		{
			int index = line.indexOf("//-");
			if(index >= 0)
			{
				expected.add(normalize(line.substring(index + 3).strip()));
			}
		}

		PsiFile file = CSharpResolveTesting.openSource(projects, MockSdk.MONO_4_5, fileName, source, List.of());

		List<String> actual = ReadAction.compute(() ->
		{
			List<String> result = new ArrayList<>();
			for(CSharpMethodCallExpressionImpl call : PsiTreeUtil.findChildrenOfType(methodM(file), CSharpMethodCallExpressionImpl.class))
			{
				result.add(normalize(display(call.resolveToCallable())));
			}
			return result;
		});

		assertThat(String.join("\n", actual)).isEqualTo(String.join("\n", expected));
	}

	@RequiredReadAction
	private static PsiElement methodM(PsiFile file)
	{
		for(CSharpTypeDeclaration type : PsiTreeUtil.findChildrenOfType(file, CSharpTypeDeclaration.class))
		{
			if("C".equals(type.getName()) && type.getParent() == file)
			{
				for(DotNetNamedElement member : type.getMembers())
				{
					if(member instanceof DotNetLikeMethodDeclaration && "M".equals(member.getName()))
					{
						return member;
					}
				}
			}
		}
		throw new AssertionError("no C.M in " + file.getName());
	}

	/**
	 * {@code C.D.N(byte)}, {@code Ref1.M(ref int)}, {@code C.N3(int, int, params int[])} - the way Roslyn displays a method
	 * symbol in these tests.
	 */
	@RequiredReadAction
	private static String display(PsiElement element)
	{
		if(!(element instanceof DotNetLikeMethodDeclaration method))
		{
			return element == null ? "unresolved" : element.getClass().getSimpleName();
		}

		StringJoiner parameters = new StringJoiner(", ", "(", ")");
		for(DotNetParameter parameter : method.getParameters())
		{
			// the type of a ref or out parameter already reads "ref int"
			String type = CSharpTypeRefPresentationUtil.buildText(parameter.toTypeRef(true), TYPE_FLAGS);
			String prefix = parameter.hasModifier(CSharpModifier.PARAMS) ? "params " : "";
			parameters.add(prefix + nullableAsQuestionMark(type));
		}

		PsiElement parent = method.getParent();
		String owner = parent instanceof DotNetTypeDeclaration type ? type.getPresentableQName() : "?";
		return owner + "." + method.getName() + parameters;
	}

	/**
	 * {@code System.Nullable<Struct<Mammal>>} as {@code Struct<Mammal>?}, at any depth.
	 */
	private static String nullableAsQuestionMark(String type)
	{
		String prefix = "System.Nullable<";
		int start;
		while((start = type.indexOf(prefix)) >= 0)
		{
			int depth = 0;
			int end = start + prefix.length() - 1;
			for(int i = end; i < type.length(); i++)
			{
				char c = type.charAt(i);
				if(c == '<')
				{
					depth++;
				}
				else if(c == '>' && --depth == 0)
				{
					end = i;
					break;
				}
			}
			type = type.substring(0, start) + type.substring(start + prefix.length(), end) + "?" + type.substring(end + 1);
		}
		return type;
	}

	/**
	 * Drops the generic arguments of the owner and the method name, keeps the parameters as they are.
	 */
	private static String normalize(String text)
	{
		int parameters = text.indexOf('(');
		String head = parameters < 0 ? text : text.substring(0, parameters);
		String tail = parameters < 0 ? "" : text.substring(parameters);

		StringBuilder builder = new StringBuilder();
		int depth = 0;
		for(char c : head.toCharArray())
		{
			if(c == '<')
			{
				depth++;
			}
			else if(c == '>')
			{
				depth--;
			}
			else if(depth == 0)
			{
				builder.append(c);
			}
		}
		return builder + tail;
	}
}
