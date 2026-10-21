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
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
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
import java.util.Set;
import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every predefined type converted to every other: Consulo must find an implicit conversion where Roslyn classifies one
 * as implicit, and an explicit one (a cast) where Roslyn has any conversion at all.
 * <p>
 * The full report goes to {@code target/roslyn-conversions-<sdk>.txt}.
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class RoslynConversionMatrixTest
{
	private static final Set<String> IMPLICIT = Set.of("IDN", "IRF", "BOX", "NUM", "NUL");

	private static final Set<String> EXPLICIT = Set.of("XRF", "UNB", "XNM", "XNL");

	// native integers have their own C# 9 rules, not looked at yet
	private static final List<String> SKIPPED = List.of("nint", "nuint", "nint?", "nuint?");

	@ParameterizedTest(name = "{0}")
	@EnumSource(MockSdk.class)
	public void predefinedConversions(MockSdk sdk, HeadlessProjects projects) throws Exception
	{
		RoslynOperatorTables tables = RoslynOperatorTables.load();

		List<String> types = new ArrayList<>(RoslynOperatorTables.TYPES);
		types.removeAll(SKIPPED);

		StringJoiner parameters = new StringJoiner(", ");
		for(int i = 0; i < types.size(); i++)
		{
			parameters.add(types.get(i) + " v" + i);
		}
		String source = "public class Test\n{\n\tpublic void test(" + parameters + ")\n\t{\n\t}\n}\n";

		PsiFile file = CSharpResolveTesting.openSource(projects, sdk, "Conversions.cs", source, List.of());

		Map<String, List<String>> mismatches = ReadAction.compute(() ->
		{
			DotNetLikeMethodDeclaration method = PsiTreeUtil.findChildOfType(file, DotNetLikeMethodDeclaration.class);
			assertThat(method).isNotNull();
			DotNetParameter[] methodParameters = method.getParameters();
			assertThat(methodParameters).hasSize(types.size());

			GlobalSearchScope scope = file.getResolveScope();
			Map<String, List<String>> result = new LinkedHashMap<>();
			for(int from = 0; from < types.size(); from++)
			{
				for(int to = 0; to < types.size(); to++)
				{
					String kind = tables.conversion(types.get(from), types.get(to));
					DotNetTypeRef fromTypeRef = methodParameters[from].toTypeRef(true);
					DotNetTypeRef toTypeRef = methodParameters[to].toTypeRef(true);

					boolean implicit = CSharpTypeUtil.isInheritable(toTypeRef, fromTypeRef, CSharpCastType.IMPLICIT, scope).isSuccess();
					boolean explicit = implicit || CSharpTypeUtil.isInheritable(toTypeRef, fromTypeRef, CSharpCastType.EXPLICIT, scope).isSuccess();

					boolean wantImplicit = IMPLICIT.contains(kind);
					boolean wantExplicit = wantImplicit || EXPLICIT.contains(kind);

					String text = types.get(from) + " -> " + types.get(to) + " (" + kind + ")";
					if(implicit != wantImplicit)
					{
						result.computeIfAbsent(wantImplicit ? "implicit missing" : "implicit, but must not be", it -> new ArrayList<>()).add(text);
					}
					else if(explicit != wantExplicit)
					{
						result.computeIfAbsent(wantExplicit ? "explicit missing" : "explicit, but must not be", it -> new ArrayList<>()).add(text);
					}
				}
			}
			return result;
		});

		StringBuilder report = new StringBuilder();
		StringBuilder summary = new StringBuilder();
		int count = 0;
		for(Map.Entry<String, List<String>> entry : mismatches.entrySet())
		{
			count += entry.getValue().size();
			report.append("== ").append(entry.getKey()).append(": ").append(entry.getValue().size()).append('\n');
			summary.append(entry.getKey()).append(": ").append(entry.getValue().size()).append(", e.g. ").append(entry.getValue().get(0)).append('\n');
			for(String line : entry.getValue())
			{
				report.append(line).append('\n');
			}
		}

		Path reportFile = Path.of("target", "roslyn-conversions-" + sdk.name().toLowerCase() + ".txt");
		Files.writeString(reportFile, report.toString());

		int total = types.size() * types.size();
		assertThat(count).as("%s: %d of %d conversions differ from Roslyn (full report in %s)%n%s", sdk, count, total, reportFile.toAbsolutePath(), summary).isZero();
	}
}
