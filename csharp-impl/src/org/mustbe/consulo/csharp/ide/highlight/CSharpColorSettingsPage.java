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

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.doc.ide.highlight.CSharpDocHighlightKey;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.OptionsBundle;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.io.FileUtil;

/**
 * @author VISTALL
 * @since 08.04.14
 */
public class CSharpColorSettingsPage implements ColorSettingsPage
{
	private static final AttributesDescriptor[] ourDescriptors = new AttributesDescriptor[]{
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
			new AttributesDescriptor("Constructor declaration", CSharpHighlightKey.CONSTRUCTOR_NAME),
			new AttributesDescriptor("Method declaration", CSharpHighlightKey.METHOD_NAME),
			new AttributesDescriptor("Delegate method name", CSharpHighlightKey.DELEGATE_METHOD_NAME),
			new AttributesDescriptor("Generic parameter name", CSharpHighlightKey.GENERIC_PARAMETER_NAME),
			new AttributesDescriptor("Extension method call", CSharpHighlightKey.EXTENSION_METHOD_CALL),
			new AttributesDescriptor("Static method call", CSharpHighlightKey.STATIC_METHOD_CALL),
			new AttributesDescriptor("Instance method call", CSharpHighlightKey.INSTANCE_METHOD_CALL),
			new AttributesDescriptor("Instance field or property", CSharpHighlightKey.INSTANCE_FIELD_OR_PROPERTY),
			new AttributesDescriptor("Static field or property", CSharpHighlightKey.STATIC_FIELD_OR_PROPERTY),
			new AttributesDescriptor("Static event", CSharpHighlightKey.STATIC_EVENT),
			new AttributesDescriptor("Instance event", CSharpHighlightKey.INSTANCE_EVENT),
			new AttributesDescriptor("Parameter", CSharpHighlightKey.PARAMETER),
			new AttributesDescriptor("Method reference", CSharpHighlightKey.METHOD_REF),
			new AttributesDescriptor("Implicit or explicit cast", CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST),
			new AttributesDescriptor("Doc comment", CSharpDocHighlightKey.DOC_COMMENT),
			new AttributesDescriptor("Doc tag", CSharpDocHighlightKey.DOC_COMMENT_TAG),
			new AttributesDescriptor("Doc attribute", CSharpDocHighlightKey.DOC_COMMENT_ATTRIBUTE),
			new AttributesDescriptor(OptionsBundle.message("options.language.defaults.dot"), CSharpHighlightKey.DOT),
			new AttributesDescriptor(OptionsBundle.message("options.language.defaults.comma"), CSharpHighlightKey.COMMA),
			new AttributesDescriptor(OptionsBundle.message("options.language.defaults.brackets"), CSharpHighlightKey.BRACKETS),
			new AttributesDescriptor(OptionsBundle.message("options.language.defaults.braces"), CSharpHighlightKey.BRACES),
			new AttributesDescriptor(OptionsBundle.message("options.language.defaults.parentheses"), CSharpHighlightKey.PARENTHESES),
			new AttributesDescriptor(OptionsBundle.message("options.language.defaults.semicolon"), CSharpHighlightKey.SEMICOLON),
			new AttributesDescriptor(OptionsBundle.message("options.language.defaults.operation"), CSharpHighlightKey.OPERATION_SIGN),
			new AttributesDescriptor("Colon", CSharpHighlightKey.COLON),
			new AttributesDescriptor("Arrows (->, =>)", CSharpHighlightKey.ARROW),
	};
	private static final Map<String, TextAttributesKey> ourAdditionalTags = new HashMap<String, TextAttributesKey>()
	{
		{
			put("class_name", CSharpHighlightKey.CLASS_NAME);
			put("attribute_name", CSharpHighlightKey.ATTRIBUTE_NAME);
			put("generic_parameter_name", CSharpHighlightKey.GENERIC_PARAMETER_NAME);
			put("delegate_method_name", CSharpHighlightKey.DELEGATE_METHOD_NAME);
			put("soft_keyword", CSharpHighlightKey.SOFT_KEYWORD);
			put("method_name", CSharpHighlightKey.METHOD_NAME);
			put("constructor_name", CSharpHighlightKey.CONSTRUCTOR_NAME);
			put("macro_keyword", CSharpHighlightKey.MACRO_KEYWORD);
			put("macro_variable", CSharpHighlightKey.MACRO_VARIABLE);
			put("disabled_block", CSharpHighlightKey.DISABLED_BLOCK);
			put("extension_call", CSharpHighlightKey.EXTENSION_METHOD_CALL);
			put("static_call", CSharpHighlightKey.STATIC_METHOD_CALL);
			put("instance_call", CSharpHighlightKey.INSTANCE_METHOD_CALL);
			put("static_field", CSharpHighlightKey.STATIC_FIELD_OR_PROPERTY);
			put("instance_field", CSharpHighlightKey.INSTANCE_FIELD_OR_PROPERTY);
			put("parameter", CSharpHighlightKey.PARAMETER);
			put("method_ref", CSharpHighlightKey.METHOD_REF);
			put("static_event_name", CSharpHighlightKey.STATIC_EVENT);
			put("instance_event_name", CSharpHighlightKey.INSTANCE_EVENT);
			put("implicit_or_explicit", CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST);
			put("doc_tag", CSharpDocHighlightKey.DOC_COMMENT_TAG);
			put("doc_attribute", CSharpDocHighlightKey.DOC_COMMENT_ATTRIBUTE);
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
	@LazyInstance
	public String getDemoText()
	{
		try
		{
			return FileUtil.loadTextAndClose(getClass().getResourceAsStream("/colorSettingsPage/C#.txt"), true);
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
}
