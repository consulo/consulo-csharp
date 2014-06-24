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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;

/**
 * @author VISTALL
 * @since 15.03.14
 */
public class CSharpOperatorHelperImpl extends CSharpOperatorHelper
{
	private static String[] ourStubs = new String[]
	{
		"/stub/ObjectStubs.cs",
		"/stub/EnumStubs.cs",
		"/stub/BoolStubs.cs",
		"/stub/StringStubs.cs",
		"/stub/ByteStubs.cs",
		"/stub/ShortStubs.cs",
		"/stub/IntStubs.cs",
		"/stub/LongStubs.cs",
		"/stub/FloatStubs.cs",
	};
	private final Project myProject;

	public CSharpOperatorHelperImpl(Project project)
	{
		myProject = project;
	}

	@NotNull
	@Override
	@LazyInstance
	public List<DotNetNamedElement> getStubMembers()
	{
		List<DotNetNamedElement> list = new ArrayList<DotNetNamedElement>();
		for(String stub : ourStubs)
		{
			InputStream resourceAsStream = getClass().getResourceAsStream(stub);
			if(resourceAsStream == null)
			{
				throw new Error("Possible broken build. '" + stub + "' not found");
			}
			try
			{
				String text = FileUtil.loadTextAndClose(resourceAsStream);
				DotNetTypeDeclaration declaration = CSharpFileFactory.createTypeDeclaration(myProject, text);
				Collections.addAll(list, declaration.getMembers());
			}
			catch(IOException e)
			{
				throw new Error("Possible broken build. '" + stub + "' not found");
			}
		}
		return list;
	}
}
