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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The base class libraries a test module is set up with, from {@code mockSdk} in the repository root.
 *
 * @author VISTALL
 */
public enum MockSdk
{
	/**
	 * Mono 4.5 mscorlib and System, decompiled to msil and zipped - primitive types have no operators of their own,
	 * so the operators come from {@code operatorStubs.xml} only. LINQ and expression trees: the System.Core reference
	 * assembly.
	 */
	MONO_4_5("mono4.5", new String[]{"mscorlib.zip", "System.zip"}, new String[]{"System.Core.dll"}),
	/**
	 * The .NET 10 reference assemblies: {@code System.Runtime.dll}, whose primitive types implement the generic math
	 * interfaces ({@code IAdditionOperators`3}, {@code IBitwiseOperators`3}...) with static abstract operators, and
	 * {@code System.Collections} and {@code System.Console}. LINQ and expression trees: {@code System.Linq}, {@code System.Linq.Expressions}.
	 */
	NET_10("net10.0", new String[]{"System.Runtime.dll", "System.Collections.dll", "System.Console.dll", "System.Runtime.InteropServices.dll"}, new String[]{"System.Linq.dll", "System.Linq.Expressions.dll"});

	private final String myDirectory;
	private final String[] myLibraries;
	private final String[] myLinqLibraries;

	MockSdk(String directory, String[] libraries, String[] linqLibraries)
	{
		myDirectory = directory;
		myLibraries = libraries;
		myLinqLibraries = linqLibraries;
	}

	/**
	 * The base class library - what every test module gets.
	 */
	public List<Path> getLibraries()
	{
		return paths(myLibraries);
	}

	/**
	 * LINQ and expression trees, for the tests which need them - the others keep the candidates of the base library
	 * only (System.Core of mono adds Action and Func of 9 to 16 type parameters).
	 */
	public List<Path> getLinqLibraries()
	{
		return paths(myLinqLibraries);
	}

	private List<Path> paths(String[] names)
	{
		// surefire runs with the module directory as the working directory
		Path directory = Path.of("").toAbsolutePath().getParent().resolve("mockSdk").resolve(myDirectory);
		assertThat(directory).isDirectory();

		List<Path> libraries = new ArrayList<>(names.length);
		for(String name : names)
		{
			libraries.add(directory.resolve(name));
		}
		return libraries;
	}
}
