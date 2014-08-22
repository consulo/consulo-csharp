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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.MultiMap;
import edu.arizona.cs.mbel.mbel.ModuleParser;
import edu.arizona.cs.mbel.mbel.TypeDef;
import edu.arizona.cs.mbel.parse.MSILParseException;
import lombok.val;

/**
 * @author VISTALL
 * @since 22.08.14
 */
public class LibrariesSearcher
{
	@LazyInstance
	@NotNull
	public static LibrariesSearcher getInstance()
	{
		return new LibrariesSearcher();
	}

	private final Map<File, MultiMap<String, String>> myCacheMap = new ConcurrentHashMap<File, MultiMap<String, String>>();

	@NotNull
	public List<Couple<String>> searchInSystemLibraries(@NotNull final PsiElement owner, String name)
	{
		DotNetModuleExtension<?> extension = ModuleUtilCore.getExtension(owner, DotNetModuleExtension.class);
		if(extension == null)
		{
			return Collections.emptyList();
		}

		List<File> availableSystemLibraries = extension.getAvailableSystemLibraries();
		if(availableSystemLibraries.isEmpty())
		{
			return Collections.emptyList();
		}

		val list = new ArrayList<Couple<String>>();

		for(val availableSystemLibrary : availableSystemLibraries)
		{
			MultiMap<String, String> map = myCacheMap.get(availableSystemLibrary);
			if(map == null)
			{
				final Ref<MultiMap<String, String>> mapRef = Ref.create();
				ApplicationManager.getApplication().invokeAndWait(new Runnable()
				{
					@Override
					public void run()
					{
						new Task.Modal(owner.getProject(), "Analyzing " + availableSystemLibrary.getName(), false)
						{
							@Override
							public void run(@NotNull ProgressIndicator progressIndicator)
							{
								mapRef.set(buildCache(availableSystemLibrary));
							}
						}.queue();
					}
				}, ModalityState.current());

				map = mapRef.get();
			}

			if(map.isEmpty())
			{
				continue;
			}
			myCacheMap.put(availableSystemLibrary, map);

			Collection<String> strings = map.get(name);
			for(String namespace : strings)
			{
				list.add(new Couple<String>(availableSystemLibrary.getName(), namespace));
			}
		}
		return list;
	}

	private MultiMap<String, String> buildCache(File file)
	{
		try
		{
			ModuleParser parser = new ModuleParser(new FileInputStream(file));

			MultiMap<String, String> map = new MultiMap<String, String>();
			TypeDef[] typeDefs = parser.getTypeDefs();

			for(TypeDef typeDef : typeDefs)
			{
				map.putValue(typeDef.getName(), typeDef.getNamespace());
			}
			return map;
		}
		catch(IOException e)
		{
		}
		catch(MSILParseException e)
		{
		}
		return MultiMap.emptyInstance();
	}
}
