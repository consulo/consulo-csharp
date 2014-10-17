package org.mustbe.consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.MemberByAllNamespaceQNameIndex;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.MemberByNamespaceQNameIndex;
import org.mustbe.consulo.dotnet.lang.psi.impl.IndexBasedDotNetNamespaceAsElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StringStubIndexExtension;

/**
 * @author VISTALL
 * @since 23.09.14
 */
public class CSharpNamespaceAsElementImpl extends IndexBasedDotNetNamespaceAsElement
{
	public CSharpNamespaceAsElementImpl(@NotNull Project project, @NotNull String indexKey, @NotNull String qName)
	{
		super(project, CSharpLanguage.INSTANCE, indexKey, qName);
	}

	@NotNull
	@Override
	public StringStubIndexExtension<? extends PsiElement> getHardIndexExtension()
	{
		return MemberByNamespaceQNameIndex.getInstance();
	}

	@NotNull
	@Override
	public StringStubIndexExtension<? extends PsiElement> getSoftIndexExtension()
	{
		return MemberByAllNamespaceQNameIndex.getInstance();
	}
}
