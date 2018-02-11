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

package consulo.csharp.ide.codeStyle;

import javax.annotation.Nonnull;

import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

/**
 * @author VISTALL
 * @since 28.07.2015
 */
public class CSharpCodeGenerationSettings extends CustomCodeStyleSettings
{
	@Nonnull
	public static CSharpCodeGenerationSettings getInstance(@Nonnull Project project)
	{
		CodeStyleSettingsManager codeStyleSettingsManager = CodeStyleSettingsManager.getInstance(project);
		return codeStyleSettingsManager.getCurrentSettings().getCustomSettings(CSharpCodeGenerationSettings.class);
	}

	// --------------- property naming ----------------------
	public String STATIC_PROPERTY_PREFIX = "";

	public String PROPERTY_PREFIX = "";

	public String STATIC_PROPERTY_SUFFIX = "";

	public String PROPERTY_SUFFIX = "";

	// --------------- field naming ----------------------
	public String STATIC_FIELD_PREFIX = "";

	public String FIELD_PREFIX = "";

	public String FIELD_SUFFIX = "";

	public String STATIC_FIELD_SUFFIX = "";

	/**
	 * Use System.String or string
	 */
	public boolean USE_LANGUAGE_DATA_TYPES = true;

	public boolean USE_VAR_FOR_EXTRACT_LOCAL_VARIABLE = true;

	public CSharpCodeGenerationSettings(CodeStyleSettings container)
	{
		super("csharp-code-generation-settings", container);
	}
}
