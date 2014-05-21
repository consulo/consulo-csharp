/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CSharpTypeDeclarationImplUtil
{
	public static void processConstructors(@NotNull DotNetTypeDeclaration type, @NotNull Processor<DotNetConstructorDeclaration> processor)
	{
		for(DotNetNamedElement dotNetNamedElement : type.getMembers())
		{
			if(!(dotNetNamedElement instanceof DotNetConstructorDeclaration))
			{
				continue;
			}

			if(!processor.process((DotNetConstructorDeclaration) dotNetNamedElement))
			{
				return;
			}
		}
	}
}
