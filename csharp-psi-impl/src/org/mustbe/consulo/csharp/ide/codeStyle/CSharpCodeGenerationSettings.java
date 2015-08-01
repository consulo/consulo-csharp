package org.mustbe.consulo.csharp.ide.codeStyle;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

/**
 * @author VISTALL
 * @since 28.07.2015
 */
public class CSharpCodeGenerationSettings extends CustomCodeStyleSettings
{
	@NotNull
	public static CSharpCodeGenerationSettings getInstance(@NotNull Project project)
	{
		CodeStyleSettingsManager codeStyleSettingsManager = CodeStyleSettingsManager.getInstance(project);
		return codeStyleSettingsManager.getCurrentSettings().getCustomSettings(CSharpCodeGenerationSettings.class);
	}

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

	/**
	 * Use System.String or string
	 */
	public boolean USE_LANGUAGE_DATA_TYPES = true;

	public CSharpCodeGenerationSettings(CodeStyleSettings container)
	{
		super("csharp-code-generation-settings", container);
	}
}
