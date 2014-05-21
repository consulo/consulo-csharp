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

package org.mustbe.consulo.csharp.ide.highlight;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.codeStyle.DisplayPriority;
import com.intellij.psi.codeStyle.DisplayPrioritySortable;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class CSharpColorSettingsPage implements ColorSettingsPage, DisplayPrioritySortable
{
	private static final AttributesDescriptor[] ourDescriptors = new AttributesDescriptor[]
			{
				new AttributesDescriptor("Block comment", CSharpHighlightKey.BLOCK_COMMENT),
				new AttributesDescriptor("Line comment", CSharpHighlightKey.LINE_COMMENT),
				new AttributesDescriptor("Keyword", CSharpHighlightKey.KEYWORD),
				new AttributesDescriptor("Context keyword", CSharpHighlightKey.SOFT_KEYWORD),
				new AttributesDescriptor("Preprocessor keyword", CSharpHighlightKey.MACRO_KEYWORD),
				new AttributesDescriptor("Preprocessor variable", CSharpHighlightKey.MACRO_VARIABLE),
				new AttributesDescriptor("Disabled preprocessor block", CSharpHighlightKey.DISABLED_BLOCK),
				new AttributesDescriptor("String", CSharpHighlightKey.STRING),
				new AttributesDescriptor("Number", CSharpHighlightKey.NUMBER),
				new AttributesDescriptor("Class name", CSharpHighlightKey.CLASS_NAME),
				new AttributesDescriptor("Generic parameter name", CSharpHighlightKey.GENERIC_PARAMETER_NAME),
				new AttributesDescriptor("Extension method call", CSharpHighlightKey.EXTENSION_METHOD),
				new AttributesDescriptor("Static method call", CSharpHighlightKey.STATIC_METHOD),
				new AttributesDescriptor("Instance method call", CSharpHighlightKey.INSTANCE_METHOD),
				new AttributesDescriptor("Instance field or property", CSharpHighlightKey.INSTANCE_FIELD),
				new AttributesDescriptor("Static field or property", CSharpHighlightKey.STATIC_FIELD),
				new AttributesDescriptor("Parameter", CSharpHighlightKey.PARAMETER),
			};
	private static final Map<String, TextAttributesKey> ourAdditionalTags = new HashMap<String, TextAttributesKey>()
	{
		{
			put("class_name", CSharpHighlightKey.CLASS_NAME);
			put("attribute_name", CSharpHighlightKey.ATTRIBUTE_NAME);
			put("generic_parameter_name", CSharpHighlightKey.GENERIC_PARAMETER_NAME);
			put("soft_keyword", CSharpHighlightKey.SOFT_KEYWORD);
			put("macro_keyword", CSharpHighlightKey.MACRO_KEYWORD);
			put("macro_variable", CSharpHighlightKey.MACRO_VARIABLE);
			put("disabled_block", CSharpHighlightKey.DISABLED_BLOCK);
			put("extension_call", CSharpHighlightKey.EXTENSION_METHOD);
			put("static_call", CSharpHighlightKey.STATIC_METHOD);
			put("instance_call", CSharpHighlightKey.INSTANCE_METHOD);
			put("static_field", CSharpHighlightKey.STATIC_FIELD);
			put("instance_field", CSharpHighlightKey.INSTANCE_FIELD);
			put("parameter", CSharpHighlightKey.PARAMETER);
		}
	};

	@Nullable
	@Override
	public Icon getIcon()
	{
		return null;
	}

	@NotNull
	@Override
	public SyntaxHighlighter getHighlighter()
	{
		return new CSharpSyntaxHighlighter();
	}

	@NotNull
	@Override
	public String getDemoText()
	{
		try
		{
			return FileUtil.loadTextAndClose(getClass().getResourceAsStream("/colorSettingsPage/C#.txt"));
		}
		catch(IOException e)
		{
			throw new Error(e);
		}
	}

	@Nullable
	@Override
	public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap()
	{
		return ourAdditionalTags;
	}

	@NotNull
	@Override
	public AttributesDescriptor[] getAttributeDescriptors()
	{
		return ourDescriptors;
	}

	@NotNull
	@Override
	public ColorDescriptor[] getColorDescriptors()
	{
		return new ColorDescriptor[0];
	}

	@NotNull
	@Override
	public String getDisplayName()
	{
		return "C#";
	}

	@Override
	public DisplayPriority getPriority()
	{
		return DisplayPriority.LANGUAGE_SETTINGS;
	}
}
