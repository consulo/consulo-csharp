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
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * C# 12 collection expressions: {@code List<string> items = [];}, {@code [..items, "last"]} - they parse, and convert to
 * the arrays and collection types they are given to.
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class CollectionExpressionTest
{
	private static final String SOURCE = """
			using System.Collections.Generic;

			namespace Cs.Items
			{
			    public class Bag
			    {
			        private readonly List<string> _items = [];

			        private int[] _numbers = [1, 2, 3];

			        public IEnumerable<string> All()
			        {
			            return [.. _items, "last"];
			        }

			        public void Add(List<string> more)
			        {
			            _items.AddRange(more);
			        }

			        public void Use()
			        {
			            Add(["a", "b"]);
			            Add([]);
			            int[] copy = [.._numbers, 4];
			            var count = copy.Length;
			        }
			    }
			}
			""";

	@ParameterizedTest
	@EnumSource(MockSdk.class)
	public void parsesAndConverts(MockSdk sdk, HeadlessProjects projects) throws Exception
	{
		PsiFile file = CSharpResolveTesting.openSource(projects, sdk, "Bag.cs", SOURCE, sdk.getLinqLibraries());

		ReadAction.run(() ->
		{
			List<String> errors = new ArrayList<>();
			for(PsiErrorElement error : PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class))
			{
				errors.add(error.getErrorDescription() + "@" + error.getTextOffset());
			}
			assertThat(errors).as("parse errors").isEmpty();

			List<String> unresolved = new ArrayList<>();
			for(CSharpReferenceExpression reference : PsiTreeUtil.findChildrenOfType(file, CSharpReferenceExpression.class))
			{
				if(reference.resolve() == null)
				{
					unresolved.add(reference.getText() + "@" + reference.getTextOffset());
				}
			}
			assertThat(unresolved).as("unresolved references on %s", sdk).isEmpty();

			// the initializers convert to the types of their variables
			List<String> notConverted = new ArrayList<>();
			for(DotNetVariable variable : PsiTreeUtil.findChildrenOfType(file, DotNetVariable.class))
			{
				DotNetExpression initializer = variable.getInitializer();
				if(initializer != null && initializer.getText().startsWith("[")
						&& !CSharpTypeUtil.isInheritable(variable.toTypeRef(false), initializer.toTypeRef(false), CSharpCastType.IMPLICIT, file.getResolveScope()).isSuccess())
				{
					notConverted.add(variable.getName());
				}
			}
			assertThat(notConverted).as("initializers which do not convert").isEmpty();

			// Add([...]) binds to Add(List<string>)
			for(CSharpCallArgumentListOwner call : PsiTreeUtil.findChildrenOfType(file, CSharpCallArgumentListOwner.class))
			{
				if(call.getText().startsWith("Add("))
				{
					PsiElement callable = call.resolveToCallable();
					assertThat(callable).as("%s on %s", call.getText(), sdk).isNotNull();
				}
			}
		});
	}
}
