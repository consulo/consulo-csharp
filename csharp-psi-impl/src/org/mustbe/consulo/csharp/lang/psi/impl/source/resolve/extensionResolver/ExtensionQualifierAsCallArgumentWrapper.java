package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.extensionResolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 29.10.14
 */
public class ExtensionQualifierAsCallArgumentWrapper extends LightElement implements CSharpCallArgument
{
	private static class LightExpression extends LightElement implements DotNetExpression
	{
		private final DotNetTypeRef myQualifierTypeRef;

		protected LightExpression(PsiManager manager, Language language, DotNetTypeRef qualifierTypeRef)
		{
			super(manager, language);
			myQualifierTypeRef = qualifierTypeRef;
		}

		@NotNull
		@Override
		public DotNetTypeRef toTypeRef(boolean b)
		{
			return myQualifierTypeRef;
		}

		@Override
		public String toString()
		{
			return "ExtensionQualifierAsCallArgumentWrapper.LightExpression";
		}
	}

	private LightExpression myExpression;

	public ExtensionQualifierAsCallArgumentWrapper(Project project, DotNetTypeRef qualifierTypeRef)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myExpression = new LightExpression(getManager(), getLanguage(), qualifierTypeRef);
	}

	@Override
	public String toString()
	{
		return "ExtensionQualifierAsCallArgumentWrapper";
	}

	@Nullable
	@Override
	public DotNetExpression getArgumentExpression()
	{
		return myExpression;
	}
}
