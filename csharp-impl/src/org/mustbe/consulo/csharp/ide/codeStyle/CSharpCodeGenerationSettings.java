package org.mustbe.consulo.csharp.ide.codeStyle;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

/**
 * @author VISTALL
 * @since 28.07.2015
 */
public class CSharpCodeGenerationSettings extends CustomCodeStyleSettings
{
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

	public CSharpCodeGenerationSettings(CodeStyleSettings container)
	{
		super("csharp-code-generation-settings", container);
	}
}
