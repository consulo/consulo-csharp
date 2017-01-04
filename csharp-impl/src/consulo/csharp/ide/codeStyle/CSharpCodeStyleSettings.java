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

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

/**
 * @author VISTALL
 * @since 18.12.14
 */
public class CSharpCodeStyleSettings extends CustomCodeStyleSettings
{
	@NotNull
	public static CSharpCodeStyleSettings getInstance(@NotNull Project project)
	{
		CodeStyleSettingsManager codeStyleSettingsManager = CodeStyleSettingsManager.getInstance(project);
		return codeStyleSettingsManager.getCurrentSettings().getCustomSettings(CSharpCodeStyleSettings.class);
	}

	// Wrapping settings
	@CommonCodeStyleSettings.BraceStyleConstant
	public int NAMESPACE_BRACE_STYLE = CommonCodeStyleSettings.END_OF_LINE;

	@CommonCodeStyleSettings.BraceStyleConstant
	public int PROPERTY_BRACE_STYLE = CommonCodeStyleSettings.END_OF_LINE;

	@CommonCodeStyleSettings.BraceStyleConstant
	public int EVENT_BRACE_STYLE = CommonCodeStyleSettings.END_OF_LINE;

	@CommonCodeStyleSettings.BraceStyleConstant
	public int INDEX_METHOD_BRACE_STYLE = CommonCodeStyleSettings.END_OF_LINE;

	public boolean KEEP_AUTO_PROPERTY_IN_ONE_LINE = true;

	// ----------------------------------- Parentheses  settings -------------------------------------
	/**
	 * "foreach (...)"
	 * or
	 * "foreach(...)"
	 */
	public boolean SPACE_BEFORE_FOREACH_PARENTHESES = true;

	/**
	 * "using (...)"
	 * or
	 * "using(...)"
	 */
	public boolean SPACE_BEFORE_USING_PARENTHESES = true;

	/**
	 * "lock (...)"
	 * or
	 * "lock(...)"
	 */
	public boolean SPACE_BEFORE_LOCK_PARENTHESES = true;

	/**
	 * "fixed (...)"
	 * or
	 * "fixed(...)"
	 */
	public boolean SPACE_BEFORE_FIXED_PARENTHESES = true;

	// ----------------------------------- Left Brace settings -------------------------------------
	/**
	 * "int MyProperty {"
	 * or
	 * "int MyProperty{"
	 */
	public boolean SPACE_BEFORE_PROPERTY_LBRACE = true;

	/**
	 * "int this[int b] {"
	 * or
	 * "int this[int b]{"
	 */
	public boolean SPACE_BEFORE_INDEX_METHOD_LBRACE = true;

	/**
	 * "event Delegate MyEvent {"
	 * or
	 * "event Delegate MyEvent{"
	 */
	public boolean SPACE_BEFORE_EVENT_LBRACE = true;

	/**
	 * "namespace Test {"
	 * or
	 * "namespace Test{"
	 */
	public boolean SPACE_BEFORE_NAMESPACE_LBRACE = true;

	/**
	 * "unsafe {"
	 * or
	 * "unsafe{"
	 */
	public boolean SPACE_BEFORE_UNSAFE_LBRACE = true;

	/**
	 * "using(...) {"
	 * or
	 * "using(...){"
	 */
	public boolean SPACE_BEFORE_USING_LBRACE = true;

	/**
	 * "lock(...) {"
	 * or
	 * "lock(...){"
	 */
	public boolean SPACE_BEFORE_LOCK_LBRACE = true;

	/**
	 * "foreach(...) {"
	 * or
	 * "foreach(...){"
	 */
	public boolean SPACE_BEFORE_FOREACH_LBRACE = true;

	/**
	 * "fixed(...) {"
	 * or
	 * "fixed(...){"
	 */
	public boolean SPACE_BEFORE_FIXED_LBRACE = true;

	public CSharpCodeStyleSettings(CodeStyleSettings container)
	{
		super("csharp-settings", container);
	}
}
