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

package consulo.csharp.ide.codeInsight;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * @author VISTALL
 * @since 01.01.14.
 */
@Singleton
@State(name = "CSharpCodeInsightSettings", storages = @Storage("editor.codeinsight.xml"))
public class CSharpCodeInsightSettings implements PersistentStateComponent<CSharpCodeInsightSettings>
{
	@Nonnull
	public static CSharpCodeInsightSettings getInstance()
	{
		return ServiceManager.getService(CSharpCodeInsightSettings.class);
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
