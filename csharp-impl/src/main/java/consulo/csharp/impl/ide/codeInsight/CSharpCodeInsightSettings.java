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

package consulo.csharp.impl.ide.codeInsight;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.Application;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.util.xml.serializer.XmlSerializerUtil;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;

/**
 * @author VISTALL
 * @since 01.01.14.
 */
@Singleton
@State(name = "CSharpCodeInsightSettings", storages = @Storage("editor.codeinsight.xml"))
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class CSharpCodeInsightSettings implements PersistentStateComponent<CSharpCodeInsightSettings>
{
	@Nonnull
	public static CSharpCodeInsightSettings getInstance()
	{
		return Application.get().getInstance(CSharpCodeInsightSettings.class);
	}

	public boolean OPTIMIZE_IMPORTS_ON_THE_FLY = true;

	@Override
	public void loadState(final CSharpCodeInsightSettings state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}

	@Override
	public CSharpCodeInsightSettings getState()
	{
		return this;
	}
}
