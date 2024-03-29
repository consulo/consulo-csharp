/*
 * Copyright 2013-2022 consulo.io
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

package consulo.csharp.impl.ide.liveTemplates;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.template.DefaultLiveTemplatesProvider;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11-Sep-22
 */
@ExtensionImpl
public class CSharpDefaultLiveTemplatesProvider implements DefaultLiveTemplatesProvider
{
	@Nonnull
	@Override
	public String[] getDefaultLiveTemplateFiles()
	{
		return new String[]{
				"/liveTemplates/output.xml",
				"/liveTemplates/main.xml",
				"/liveTemplates/foreach.xml",
				"/liveTemplates/linq.xml"
		};
	}
}
