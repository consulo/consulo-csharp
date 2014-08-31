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

package org.mustbe.consulo.csharp.ide.assemblyInfo.blocks;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 09.03.14
 */
public class CSharpSimpleStringAssemblyBlock extends CSharpAssemblyBlock
{
	public CSharpSimpleStringAssemblyBlock(String title, String attributeType)
	{
		super(title, attributeType);
	}

	@Override
	public JComponent createAndLoadComponent(PsiFile file, boolean mutable)
	{
		JTextArea comp = new JTextArea();
		comp.setEditable(mutable);

		String value = getValue(file, String.class);
		if(value != null)
		{
			comp.setText(StringUtil.unescapeSlashes(value));
		}
		return comp;
	}
}
