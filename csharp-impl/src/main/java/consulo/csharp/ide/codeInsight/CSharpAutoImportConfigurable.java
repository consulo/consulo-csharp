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

import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.options.Configurable;
import consulo.options.SimpleConfigurableByProperties;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;

/**
 * @author VISTALL
 * @since 01.01.14.
 */
public class CSharpAutoImportConfigurable extends SimpleConfigurableByProperties implements Configurable
{
	@RequiredUIAccess
	@Nonnull
	@Override
	protected Component createLayout(PropertyBuilder propertyBuilder)
	{
		VerticalLayout verticalLayout = VerticalLayout.create();

		CSharpCodeInsightSettings settings = CSharpCodeInsightSettings.getInstance();

		CheckBox optimizeImportOnTheFlyBox = CheckBox.create(ApplicationBundle.message("checkbox.optimize.imports.on.the.fly"));
		verticalLayout.add(optimizeImportOnTheFlyBox);
		propertyBuilder.add(optimizeImportOnTheFlyBox, () -> settings.OPTIMIZE_IMPORTS_ON_THE_FLY, value -> settings.OPTIMIZE_IMPORTS_ON_THE_FLY = value);

		return verticalLayout;
	}
}
