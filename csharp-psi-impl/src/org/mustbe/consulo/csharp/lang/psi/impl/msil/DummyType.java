package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing.SomeType;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing.SomeTypeParser;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public class DummyType extends LightElement implements DotNetType
{
	private final PsiElement myScope;
	private final SomeType mySomeType;

	public DummyType(Project project, PsiElement scope, SomeType someType)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myScope = scope;
		mySomeType = someType;
	}

	@Override
	public String toString()
	{
		return "DummyType";
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		return SomeTypeParser.convert(mySomeType, myScope);
	}
}
