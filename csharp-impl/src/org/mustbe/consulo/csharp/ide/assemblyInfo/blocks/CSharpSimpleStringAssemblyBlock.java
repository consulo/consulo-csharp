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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.intellij.ui.components.JBTextField;

/**
 * @author VISTALL
 * @since 09.03.14
 */
public class CSharpSimpleStringAssemblyBlock implements CSharpAssemblyBlock
{
	private final String myTitle;
	private final String myAttributeType;

	public CSharpSimpleStringAssemblyBlock(String title, String attributeType)
	{
		myTitle = title;
		myAttributeType = attributeType;
	}

	@Override
	public JComponent createComponent()
	{
		JPanel panel = new JPanel(new BorderLayout());

		panel.setBorder(new TitledBorder(myTitle));
		JBTextField comp = new JBTextField();
		comp.setPreferredSize(new Dimension(250, comp.getMinimumSize().height));
		panel.add(comp);
		return panel;
	}
}
