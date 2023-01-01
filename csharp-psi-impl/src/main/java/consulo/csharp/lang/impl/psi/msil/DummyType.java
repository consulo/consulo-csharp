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

package consulo.csharp.lang.impl.psi.msil;

import javax.annotation.Nonnull;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.msil.typeParsing.SomeType;
import consulo.csharp.lang.impl.psi.msil.typeParsing.SomeTypeParser;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.impl.psi.LightElement;
import consulo.project.Project;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public class DummyType extends LightElement implements DotNetType
{
	private final PsiElement myScope;
	private final SomeType mySomeType;

	public DummyType(Project project, PsiElement scope, SomeType someType)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myScope = scope;
		mySomeType = someType;
	}

	@Override
	public String toString()
	{
		return "DummyType";
	}

	@Nonnull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		return SomeTypeParser.convert(mySomeType, myScope);
	}
}
