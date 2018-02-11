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

import javax.annotation.Nullable;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.Nls;
import com.intellij.application.options.codeStyle.CommenterForm;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import consulo.annotations.RequiredDispatchThread;
import consulo.csharp.lang.CSharpLanguage;

/**
 * @author VISTALL
 * @since 28.07.2015
 */
public class CSharpCodeGenerationSettingsConfigurable implements Configurable
{
	private CSharpCodeGenerationSettings mySettings;
	private JTextField myFieldPrefixField;
	private JTextField myStaticFieldPrefixField;
	private JTextField myPropertyPrefixField;
	private JTextField myStaticPropertyPrefixField;
	private JTextField myFieldSuffixField;
	private JTextField myStaticFieldSuffixField;
	private JTextField myPropertySuffixField;
	private JTextField myStaticPropertySuffixField;
	private JCheckBox myUseLanguageKeywordsCheckBox;
	private JPanel myRoot;
	private JPanel myAdditionalRootPanel;

	private CommenterForm myCommenterForm;

	public CSharpCodeGenerationSettingsConfigurable(CodeStyleSettings settings)
	{
		mySettings = settings.getCustomSettings(CSharpCodeGenerationSettings.class);
	}

	@Nls
	@Override
	public String getDisplayName()
	{
		return ApplicationBundle.message("title.code.generation");
	}

	@RequiredDispatchThread
	@Nullable
	@Override
	public JComponent createComponent()
	{
		return myRoot;
	}

	@RequiredDispatchThread
	@Override
	public boolean isModified()
	{
		boolean isModified = isModified(myFieldPrefixField, mySettings.FIELD_PREFIX);
		isModified |= isModified(myStaticFieldPrefixField, mySettings.STATIC_FIELD_PREFIX);
		isModified |= isModified(myPropertyPrefixField, mySettings.PROPERTY_PREFIX);
		isModified |= isModified(myStaticPropertyPrefixField, mySettings.STATIC_PROPERTY_PREFIX);

		isModified |= isModified(myFieldSuffixField, mySettings.FIELD_SUFFIX);
		isModified |= isModified(myStaticFieldSuffixField, mySettings.STATIC_FIELD_SUFFIX);
		isModified |= isModified(myPropertySuffixField, mySettings.PROPERTY_SUFFIX);
		isModified |= isModified(myStaticPropertySuffixField, mySettings.STATIC_PROPERTY_SUFFIX);
		isModified |= myUseLanguageKeywordsCheckBox.isSelected() != mySettings.USE_LANGUAGE_DATA_TYPES;
		isModified |= myCommenterForm.isModified(mySettings.getContainer());
		return isModified;
	}

	private static boolean isModified(JTextField textField, String value)
	{
		return !textField.getText().trim().equals(value);
	}

	@RequiredDispatchThread
	@Override
	public void apply() throws ConfigurationException
	{
		mySettings.FIELD_PREFIX = myFieldPrefixField.getText().trim();
		mySettings.STATIC_FIELD_PREFIX = myStaticFieldPrefixField.getText().trim();
		mySettings.PROPERTY_PREFIX = myPropertyPrefixField.getText().trim();
		mySettings.STATIC_PROPERTY_PREFIX = myStaticPropertyPrefixField.getText().trim();

		mySettings.FIELD_SUFFIX = myFieldSuffixField.getText().trim();
		mySettings.STATIC_FIELD_SUFFIX = myStaticFieldSuffixField.getText().trim();
		mySettings.PROPERTY_SUFFIX = myPropertySuffixField.getText().trim();
		mySettings.STATIC_PROPERTY_SUFFIX = myStaticPropertySuffixField.getText().trim();
		mySettings.USE_LANGUAGE_DATA_TYPES = myUseLanguageKeywordsCheckBox.isSelected();
		myCommenterForm.apply(mySettings.getContainer());
	}

	@RequiredDispatchThread
	@Override
	public void reset()
	{
		myFieldPrefixField.setText(mySettings.FIELD_PREFIX);
		myStaticFieldPrefixField.setText(mySettings.STATIC_FIELD_PREFIX);
		myPropertyPrefixField.setText(mySettings.PROPERTY_PREFIX);
		myStaticPropertyPrefixField.setText(mySettings.STATIC_PROPERTY_PREFIX);

		myFieldSuffixField.setText(mySettings.FIELD_SUFFIX);
		myStaticFieldSuffixField.setText(mySettings.STATIC_FIELD_SUFFIX);
		myPropertySuffixField.setText(mySettings.PROPERTY_SUFFIX);
		myStaticPropertySuffixField.setText(mySettings.STATIC_PROPERTY_SUFFIX);
		myUseLanguageKeywordsCheckBox.setSelected(mySettings.USE_LANGUAGE_DATA_TYPES);
		myCommenterForm.reset(mySettings.getContainer());
	}

	private void createUIComponents()
	{
		myAdditionalRootPanel = new JPanel(new VerticalFlowLayout());

		myCommenterForm = new CommenterForm(CSharpLanguage.INSTANCE);
		myAdditionalRootPanel.add(myCommenterForm.getCommenterPanel());
	}
}
