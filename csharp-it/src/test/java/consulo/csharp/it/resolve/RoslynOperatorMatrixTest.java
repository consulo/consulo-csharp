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

import consulo.application.ReadAction;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.CSharpExpressionWithOperatorImpl;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every predefined type against every other with one operator of each Roslyn table, and the unary operators against
 * every type: the operator Consulo resolves must have the operand types of the one Roslyn picks.
 * <p>
 * The full report goes to {@code target/roslyn-operators-<sdk>.txt}.
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class RoslynOperatorMatrixTest
{
	private record Case(String text, String expected)
	{
	}

	/**
	 * operator -> Roslyn table; the arithmetic table is shared by * and the comparisons, which are separate operators in
	 * operatorStubs.xml
	 */
	private static final Map<String, String> BINARY = new LinkedHashMap<>();
	private static final Map<String, String> UNARY = new LinkedHashMap<>();

	static
	{
		BINARY.put("*", "s_arithmetic");
		BINARY.put("<", "s_arithmetic");
		BINARY.put("+", "s_addition");
		BINARY.put("<<", "s_shift");
		BINARY.put("==", "s_equality");
		BINARY.put("&", "s_logical");

		UNARY.put("+", "s_plus");
		UNARY.put("-", "s_minus");
		UNARY.put("~", "s_bitwiseComplement");
		UNARY.put("!", "s_logicalNegation");
	}

	// native integers have their own C# 9 rules, not looked at yet
	private static final List<String> SKIPPED = List.of("nint", "nuint", "nint?", "nuint?");

	private static final int TYPE_FLAGS = CSharpTypeRefPresentationUtil.QUALIFIED_NAME | CSharpTypeRefPresentationUtil.FORCE_TYPE_KEYWORD;

	@ParameterizedTest(name = "{0}")
	@EnumSource(MockSdk.class)
	public void predefinedOperators(MockSdk sdk, HeadlessProjects projects) throws Exception
	{
		RoslynOperatorTables tables = RoslynOperatorTables.load();

		List<String> types = new ArrayList<>(RoslynOperatorTables.TYPES);
		types.removeAll(SKIPPED);

		List<Case> cases = new ArrayList<>();
		StringBuilder source = new StringBuilder("public class Test\n{\n");
		for(Map.Entry<String, String> operator : BINARY.entrySet())
		{
			source.append("\tpublic void test").append(cases.size()).append("(").append(parameters(types)).append(")\n\t{\n");
			for(String left : types)
			{
				for(String right : types)
				{
					String kind = tables.binary(operator.getValue(), left, right);
					boolean shift = operator.getValue().equals("s_shift");
					source.append("\t\tvar e").append(cases.size()).append(" = ").append(variable(types, left)).append(' ')
							.append(operator.getKey()).append(' ').append(variable(types, right)).append(";\n");
					cases.add(new Case(left + " " + operator.getKey() + " " + right, expectedBinary(kind, shift)));
				}
			}
			source.append("\t}\n");
		}
		for(Map.Entry<String, String> operator : UNARY.entrySet())
		{
			source.append("\tpublic void test").append(cases.size()).append("(").append(parameters(types)).append(")\n\t{\n");
			for(String operand : types)
			{
				String kind = tables.unary(operator.getValue(), operand);
				source.append("\t\tvar e").append(cases.size()).append(" = ").append(operator.getKey()).append(variable(types, operand)).append(";\n");
				String type = RoslynOperatorTables.kindType(kind);
				cases.add(new Case(operator.getKey() + operand, type == null ? "unresolved" : "(" + type + ")"));
			}
			source.append("\t}\n");
		}
		source.append("}\n");

		PsiFile file = CSharpResolveTesting.openSource(projects, sdk, "Operators.cs", source.toString(), List.of());

		List<String> actual = ReadAction.compute(() ->
		{
			List<String> result = new ArrayList<>();
			for(CSharpExpressionWithOperatorImpl expression : PsiTreeUtil.findChildrenOfType(file, CSharpExpressionWithOperatorImpl.class))
			{
				result.add(operands(expression.getOperatorElement().resolve()));
			}
			return result;
		});
		assertThat(actual).hasSize(cases.size());

		Map<String, List<String>> mismatches = new LinkedHashMap<>();
		int count = 0;
		for(int i = 0; i < cases.size(); i++)
		{
			Case c = cases.get(i);
			if(!c.expected().equals(actual.get(i)))
			{
				String operator = c.text().split(" ").length == 3 ? c.text().split(" ")[1] : "unary " + c.text().charAt(0);
				mismatches.computeIfAbsent(operator, it -> new ArrayList<>()).add(c.text() + ": want " + c.expected() + ", got " + actual.get(i));
				count++;
			}
		}

		StringBuilder report = new StringBuilder();
		StringBuilder summary = new StringBuilder();
		for(Map.Entry<String, List<String>> entry : mismatches.entrySet())
		{
			report.append("== ").append(entry.getKey()).append(": ").append(entry.getValue().size()).append('\n');
			summary.append(entry.getKey()).append(": ").append(entry.getValue().size()).append(", e.g. ").append(entry.getValue().get(0)).append('\n');
			for(String line : entry.getValue())
			{
				report.append(line).append('\n');
			}
		}

		Path reportFile = Path.of("target", "roslyn-operators-" + sdk.name().toLowerCase() + ".txt");
		Files.writeString(reportFile, report.toString());

		assertThat(count).as("%s: %d of %d operators differ from Roslyn (full report in %s)%n%s", sdk, count, cases.size(), reportFile.toAbsolutePath(), summary).isZero();
	}

	private static String expectedBinary(String kind, boolean shift)
	{
		switch(kind)
		{
			case "ERR":
				return "unresolved";
			case "OBJ":
				return "(object, object)";
			case "STR":
				return "(string, string)";
			case "OSC":
				return "(object, string)";
			case "SOC":
				return "(string, object)";
		}

		String type = RoslynOperatorTables.kindType(kind);
		assertThat(type).as(kind).isNotNull();
		if(shift)
		{
			return "(" + type + ", " + (type.endsWith("?") ? "int?" : "int") + ")";
		}
		return "(" + type + ", " + type + ")";
	}

	private static String operands(PsiElement element)
	{
		if(element == null)
		{
			return "unresolved";
		}
		if(!(element instanceof DotNetLikeMethodDeclaration method))
		{
			return element.getClass().getSimpleName();
		}

		StringJoiner joiner = new StringJoiner(", ", "(", ")");
		for(DotNetTypeRef typeRef : method.getParameterTypeRefs())
		{
			String text = CSharpTypeRefPresentationUtil.buildText(typeRef, TYPE_FLAGS);
			joiner.add(text.replaceAll("^System\\.Nullable<(.*)>$", "$1?"));
		}
		return joiner.toString();
	}

	private static String parameters(List<String> types)
	{
		StringJoiner joiner = new StringJoiner(", ");
		for(String type : types)
		{
			joiner.add(type + " " + variable(types, type));
		}
		return joiner.toString();
	}

	private static String variable(List<String> types, String type)
	{
		return "v" + types.indexOf(type);
	}
}
