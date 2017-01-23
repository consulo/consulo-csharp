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

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializationException;
import com.intellij.util.xmlb.XmlSerializer;

/**
 * @author VISTALL
 * @since 01.01.14.
 */
@State(
		name = "CSharpCodeInsightSettings",
		storages = {
				@Storage(
						file = StoragePathMacros.APP_CONFIG + "/editor.codeinsight.xml")
		})
public class CSharpCodeInsightSettings implements PersistentStateComponent<Element>
{
	private static final Logger LOGGER = Logger.getInstance(CSharpCodeInsightSettings.class);

	@NotNull
	public static CSharpCodeInsightSettings getInstance()
	{
		return ServiceManager.getService(CSharpCodeInsightSettings.class);
	}

	public boolean OPTIMIZE_IMPORTS_ON_THE_FLY = true;

	@Override
	public void loadState(final Element state)
	{
		try
		{
			XmlSerializer.deserializeInto(this, state);
		}
		catch(XmlSerializationException e)
		{
			CSharpCodeInsightSettings.LOGGER.info(e);
		}
	}

	@Override
	public Element getState()
	{
		Element element = new Element("state");
		try
		{
			XmlSerializer.serializeInto(this, element);
		}
		catch(XmlSerializationException e)
		{
			CSharpCodeInsightSettings.LOGGER.info(e);
		}
		return element;
	}
}
