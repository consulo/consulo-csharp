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

package consulo.csharp.lang.impl.psi.source.resolve.extensionResolver;

import javax.annotation.Nullable;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.impl.psi.light.CSharpLightExpression;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiManager;
import consulo.project.Project;

/**
 * @author VISTALL
 * @since 29.10.14
 */
public class ExtensionQualifierAsCallArgumentWrapper extends LightElement implements CSharpCallArgument
{
	private CSharpLightExpression myExpression;

	public ExtensionQualifierAsCallArgumentWrapper(Project project, DotNetTypeRef qualifierTypeRef)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myExpression = new CSharpLightExpression(getManager(), qualifierTypeRef);
	}

	@Override
	public String toString()
	{
		return "ExtensionQualifierAsCallArgumentWrapper";
	}

	@Nullable
	@Override
	public DotNetExpression getArgumentExpression()
	{
		return myExpression;
	}
}
