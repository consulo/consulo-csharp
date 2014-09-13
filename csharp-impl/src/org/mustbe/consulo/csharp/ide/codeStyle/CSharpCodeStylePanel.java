package org.mustbe.consulo.csharp.ide.codeStyle;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.CSharpEditorHighlighter;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.psi.codeStyle.CodeStyleSettings;

/**
 * @author VISTALL
 * @since 13.09.14
 */
public class CSharpCodeStylePanel extends CodeStyleAbstractPanel
{
	public CSharpCodeStylePanel(@NotNull CodeStyleSettings settings)
	{
		super(settings);
	}

	@Override
	protected int getRightMargin()
	{
		return 60;
	}

	@Nullable
	@Override
	protected EditorHighlighter createHighlighter(EditorColorsScheme scheme)
	{
		return new CSharpEditorHighlighter(null, scheme);
	}

	@NotNull
	@Override
	protected FileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}

	@Nullable
	@Override
	protected String getPreviewText()
	{
		return readFromFile(CSharpCodeStylePanel.class, "C#.txt");
	}

	@Override
	public void apply(CodeStyleSettings settings) throws ConfigurationException
	{

	}

	@Override
	public boolean isModified(CodeStyleSettings settings)
	{
		return false;
	}

	@Nullable
	@Override
	public JComponent getPanel()
	{
		return new JPanel();
	}

	@Override
	protected void resetImpl(CodeStyleSettings settings)
	{

	}
}
