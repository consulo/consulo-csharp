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

package org.mustbe.consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.QualifiedName;

/**
 * @author VISTALL
 * @since 28.12.13.
 */
@Deprecated
public class CSharpNamespaceHelper
{
	public static final String ROOT = "<root>";
	public static final String NAMESPACE_SEPARATOR = ".";

	@Nullable
	public static CSharpNamespaceAsElement getNamespaceElementIfFind(@NotNull Project project, @NotNull final String qName,
			@NotNull GlobalSearchScope globalSearchScope)
	{
		assert !qName.isEmpty() : "Dont use empty namespace name. Use 'ROOT' field";

		return null;
	}

	@NotNull
	public static String toString(@NotNull QualifiedName namespace)
	{
		return namespace == QualifiedName.ROOT ? ROOT : namespace.toString();
	}

	@NotNull
	public static String getNamespaceForIndexing(@Nullable String namespace)
	{
		if(StringUtil.isEmpty(namespace))
		{
			return ROOT;
		}
		return namespace;
	}

	@NotNull
	public static String getNameWithNamespaceForIndexing(@Nullable String namespace, @NotNull String name)
	{
		if(StringUtil.isEmpty(namespace))
		{
			return name;
		}
		return namespace + "." + name;
	}
}
