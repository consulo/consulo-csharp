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
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code namespace N;} - the types of the file are in N, so they resolve by their names in the file.
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class FileScopedNamespaceTest
{
	@ParameterizedTest
	@EnumSource(MockSdk.class)
	public void typesOfTheNamespace(MockSdk sdk, HeadlessProjects projects) throws Exception
	{
		String catalog = """
				using System.Collections.Generic;

				namespace Cs.Services;

				public interface IShape { }

				public sealed class ShapeCatalog
				{
				    private readonly List<IShape> _shapes = new List<IShape>();

				    public ShapeCatalog Add(IShape shape) { _shapes.Add(shape); return this; }

				    public static ShapeCatalog Create() { return new ShapeCatalog(); }

				    public Counter Count() { return new Counter(); }
				}

				public class Counter { }
				""";

		PsiFile file = CSharpResolveTesting.openSource(projects, sdk, "ShapeCatalog.cs", catalog, sdk.getLinqLibraries());

		List<String> unresolved = ReadAction.compute(() ->
		{
			List<String> result = new ArrayList<>();
			for(CSharpReferenceExpression reference : PsiTreeUtil.findChildrenOfType(file, CSharpReferenceExpression.class))
			{
				if(reference.resolve() == null)
				{
					result.add(reference.getText() + "@" + reference.getTextOffset());
				}
			}
			return result;
		});

		assertThat(unresolved).as("unresolved references on %s", sdk).isEmpty();
	}
}
