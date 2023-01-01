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

package consulo.csharp.impl.ide.actions;

import consulo.annotation.component.ExtensionImpl;
import consulo.fileTemplate.FileTemplateContributor;
import consulo.fileTemplate.FileTemplateRegistrator;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11-Sep-22
 */
@ExtensionImpl
public class CSharpFileTemplateContributor implements FileTemplateContributor
{
	@Override
	public void register(@Nonnull FileTemplateRegistrator fileTemplateRegistrator)
	{
		fileTemplateRegistrator.registerInternalTemplate("CSharpClass");
		fileTemplateRegistrator.registerInternalTemplate("CSharpInterface");
		fileTemplateRegistrator.registerInternalTemplate("CSharpEnum");
		fileTemplateRegistrator.registerInternalTemplate("CSharpAttribute");
		fileTemplateRegistrator.registerInternalTemplate("CSharpStruct");
		fileTemplateRegistrator.registerInternalTemplate("CSharpFile");
		fileTemplateRegistrator.registerInternalTemplate("CSharpAssemblyFile");
	}
}
