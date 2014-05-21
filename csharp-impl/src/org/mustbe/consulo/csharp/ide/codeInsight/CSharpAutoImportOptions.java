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

package org.mustbe.consulo.csharp.ide.codeInsight;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.jetbrains.annotations.Nullable;
import com.intellij.application.options.editor.AutoImportOptionsProvider;
import com.intellij.openapi.options.ConfigurationException;

/**
 * @author VISTALL
 * @since 01.01.14.
 */
public class CSharpAutoImportOptions implements AutoImportOptionsProvider
{
	private JCheckBox myCbOptimizeImports;
	private JPanel myWholePanel;

	@Nullable
	@Override
	public JComponent createComponent()
	{
		return myWholePanel;
	}

	@Override
	public boolean isModified()
	{
		CSharpCodeInsightSettings codeInsightSettings = CSharpCodeInsightSettings.getInstance();

		boolean isModified = isModified(myCbOptimizeImports, codeInsightSettings.OPTIMIZE_IMPORTS_ON_THE_FLY);

		return isModified;
	}

	@Override
	public void apply() throws ConfigurationException
	{
		CSharpCodeInsightSettings codeInsightSettings = CSharpCodeInsightSettings.getInstance();

		codeInsightSettings.OPTIMIZE_IMPORTS_ON_THE_FLY = myCbOptimizeImports.isSelected();
	}

	@Override
	public void reset()
	{
		CSharpCodeInsightSettings codeInsightSettings = CSharpCodeInsightSettings.getInstance();

		myCbOptimizeImports.setSelected(codeInsightSettings.OPTIMIZE_IMPORTS_ON_THE_FLY);
	}

	@Override
	public void disposeUIResources()
	{

	}

	private static boolean isModified(JToggleButton checkBox, boolean value)
	{
		return checkBox.isSelected() != value;
	}
}
