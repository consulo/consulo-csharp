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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldOrPropertySet;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightFieldDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.resolve.SimpleTypeResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 08.05.14
 */
public class CSharpAnonymTypeRef extends DotNetTypeRef.Adapter
{
	private final PsiFile myContainingFile;
	private CSharpFieldOrPropertySet[] mySets;

	public CSharpAnonymTypeRef(PsiFile containingFile, CSharpFieldOrPropertySet[] sets)
	{
		myContainingFile = containingFile;
		mySets = sets;
	}

	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull PsiElement scope)
	{
		return resolve();
	}

	@LazyInstance
	@NotNull
	public DotNetTypeResolveResult resolve()
	{
		return new SimpleTypeResolveResult(createTypeDeclaration());
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		for(int i = 0; i < mySets.length; i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}
			CSharpFieldOrPropertySet set = mySets[i];
			builder.append(set.getNameReferenceExpression().getText());
		}
		builder.append("}");
		return builder.toString();
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return getPresentableText();
	}

	@NotNull
	private DotNetTypeDeclaration createTypeDeclaration()
	{
		CSharpLightTypeDeclarationBuilder builder = new CSharpLightTypeDeclarationBuilder(myContainingFile.getProject());
		builder.addModifier(CSharpModifier.PUBLIC);
		builder.withParent(myContainingFile);
		builder.withType(CSharpLightTypeDeclarationBuilder.Type.STRUCT);
		builder.addExtendType(new CSharpTypeRefByQName(DotNetTypes.System.ValueType));

		for(CSharpFieldOrPropertySet set : mySets)
		{
			DotNetExpression nameReferenceExpression = set.getNameReferenceExpression();
			DotNetExpression valueReferenceExpression = set.getValueReferenceExpression();

			CSharpLightFieldDeclarationBuilder fieldBuilder = new CSharpLightFieldDeclarationBuilder(myContainingFile.getProject());

			if(valueReferenceExpression == null)
			{
				fieldBuilder.withTypeRef(new CSharpTypeRefByQName(DotNetTypes.System.Object));
			}
			else
			{
				fieldBuilder.withTypeRef(valueReferenceExpression.toTypeRef(true));
			}
			fieldBuilder.addModifier(CSharpModifier.PUBLIC);
			fieldBuilder.withNameIdentifier(nameReferenceExpression);
			fieldBuilder.setNavigationElement(nameReferenceExpression);
			builder.addMember(fieldBuilder);
		}

		return builder;
	}
}
