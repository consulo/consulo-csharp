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
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
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
 * C# 8 switch expressions: {@code shape switch { Circle c => c.Radius, _ => 0 }} parses, its pattern variables resolve in
 * their arm, and it has the best common type of its arms.
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class SwitchExpressionTest
{
	private static final String SOURCE = """
			using System;

			namespace Cs.Shapes
			{
			    public abstract class Shape { }

			    public sealed class Circle : Shape { public double Radius; }

			    public sealed class Square : Shape { public double Side; }

			    public enum Kind { Round, Angular }

			    public static class ShapeOps
			    {
			        public static double Area(Shape shape)
			        {
			            var area = shape switch
			            {
			                Circle c when c.Radius > 0 => Math.PI * c.Radius * c.Radius,
			                Square s => s.Side * s.Side,
			                null => throw new ArgumentNullException(nameof(shape)),
			                _ => 0
			            };
			            return area;
			        }

			        public static string Name(Kind kind) => kind switch
			        {
			            Kind.Round => "round",
			            Kind.Angular => "angular",
			            _ => throw new ArgumentOutOfRangeException(nameof(kind)),
			        };

			        public static int Bucket(int value)
			        {
			            int bucket = value switch { < 0 => -1, 0 => 0, var v => v / 10 };
			            return bucket + 1;
			        }

			        public static bool IsRound(Shape shape) => shape switch { Circle => true, _ => false };

			        public static Shape Require(Shape shape) => shape ?? throw new ArgumentNullException(nameof(shape));
			    }
			}
			""";

	@ParameterizedTest
	@EnumSource(MockSdk.class)
	public void parsesAndResolves(MockSdk sdk, HeadlessProjects projects) throws Exception
	{
		PsiFile file = CSharpResolveTesting.openSource(projects, sdk, "Shapes.cs", SOURCE, sdk.getLinqLibraries());

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

			assertThat(variableType(file, "area")).isEqualTo("double");
			assertThat(variableType(file, "bucket")).isEqualTo("int");
			assertThat(expressionType(file, "kind switch")).isEqualTo("string");
			assertThat(expressionType(file, "shape switch { Circle")).isEqualTo("bool");
			assertThat(variableType(file, "c")).isEqualTo("Cs.Shapes.Circle");
			assertThat(variableType(file, "v")).isEqualTo("int");
		});
	}

	@RequiredReadAction
	private static String variableType(PsiFile file, String name)
	{
		for(DotNetVariable variable : PsiTreeUtil.findChildrenOfType(file, DotNetVariable.class))
		{
			if(name.equals(variable.getName()))
			{
				return CSharpTypeRefPresentationUtil.buildText(variable.toTypeRef(true), CSharpTypeRefPresentationUtil.QUALIFIED_NAME | CSharpTypeRefPresentationUtil.FORCE_TYPE_KEYWORD);
			}
		}
		throw new AssertionError("no variable " + name);
	}

	@RequiredReadAction
	private static String expressionType(PsiFile file, String prefix)
	{
		for(DotNetExpression expression : PsiTreeUtil.findChildrenOfType(file, DotNetExpression.class))
		{
			if(expression.getText().startsWith(prefix))
			{
				return CSharpTypeRefPresentationUtil.buildText(expression.toTypeRef(true), CSharpTypeRefPresentationUtil.QUALIFIED_NAME | CSharpTypeRefPresentationUtil.FORCE_TYPE_KEYWORD);
			}
		}
		throw new AssertionError("no expression " + prefix);
	}
}
