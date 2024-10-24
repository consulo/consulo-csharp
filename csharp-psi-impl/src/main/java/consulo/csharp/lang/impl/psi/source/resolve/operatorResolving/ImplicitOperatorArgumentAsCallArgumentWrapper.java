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

package consulo.csharp.lang.impl.psi.source.resolve.operatorResolving;

import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.impl.psi.light.CSharpLightExpression;
import consulo.document.util.TextRange;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiManager;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 13.12.14
 */
public class ImplicitOperatorArgumentAsCallArgumentWrapper extends LightElement implements CSharpCallArgument
{
	private static class LightExpression extends CSharpLightExpression
	{
		private final DotNetExpression myOriginalExpression;

		protected LightExpression(PsiManager manager, DotNetTypeRef typeRef, DotNetExpression originalExpression)
		{
			super(manager, typeRef);
			myOriginalExpression = originalExpression;
		}

		@Override
		public TextRange getTextRange()
		{
			return myOriginalExpression.getTextRange();
		}
	}

	private LightExpression myExpression;

	public ImplicitOperatorArgumentAsCallArgumentWrapper(@Nonnull DotNetExpression originalExpression, @Nonnull DotNetTypeRef implicitTypeRef)
	{
		super(PsiManager.getInstance(originalExpression.getProject()), CSharpLanguage.INSTANCE);
		myExpression = new LightExpression(getManager(), implicitTypeRef, originalExpression);
	}

	@Override
	public String toString()
	{
		return "ImplicitOperatorArgumentAsCallArgumentWrapper";
	}

	@Nullable
	@Override
	public DotNetExpression getArgumentExpression()
	{
		return myExpression;
	}
}