package org.mustbe.consulo.csharp.lang;

import org.mustbe.consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class CSharpNamesValidator implements NamesValidator
{
	@Override
	public boolean isKeyword(String name, Project project)
	{
		return CSharpNameSuggesterUtil.isKeyword(name);
	}

	@Override
	public boolean isIdentifier(String name, Project project)
	{
		return CSharpNameSuggesterUtil.isIdentifier(name);
	}
}
