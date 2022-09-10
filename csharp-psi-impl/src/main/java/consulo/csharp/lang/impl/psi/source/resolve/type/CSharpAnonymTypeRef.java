/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightFieldDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightTypeDeclarationBuilder;
import consulo.csharp.lang.psi.CSharpAnonymFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpNamedFieldOrPropertySet;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.dotnet.psi.resolve.SimpleTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 08.05.14
 */
public class CSharpAnonymTypeRef extends DotNetTypeRefWithCachedResult
{
	public static class SetField extends CSharpLightFieldDeclarationBuilder
	{
		private CSharpFieldOrPropertySet mySet;

		public SetField(Project project, CSharpFieldOrPropertySet set)
		{
			super(project);
			mySet = set;
		}

		@Override
		public boolean isEquivalentTo(PsiElement another)
		{
			if(another instanceof SetField)
			{
				return mySet.isEquivalentTo(((SetField) another).getSet());
			}
			return super.isEquivalentTo(another);
		}

		public CSharpFieldOrPropertySet getSet()
		{
			return mySet;
		}
	}

	private final PsiFile myContainingFile;
	private final CSharpFieldOrPropertySet[] mySets;

	public CSharpAnonymTypeRef(PsiFile containingFile, CSharpFieldOrPropertySet[] sets)
	{
		super(containingFile.getProject(), containingFile.getResolveScope());
		myContainingFile = containingFile;
		mySets = sets;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return new SimpleTypeResolveResult(createTypeDeclaration());
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getVmQName()
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
			builder.append(set.getNameElement().getText());
		}
		builder.append("}");
		return builder.toString();
	}

	@Nonnull
	@RequiredReadAction
	private DotNetTypeDeclaration createTypeDeclaration()
	{
		CSharpLightTypeDeclarationBuilder builder = new CSharpLightTypeDeclarationBuilder(getProject(), getResolveScope());
		builder.addModifier(CSharpModifier.PUBLIC);
		builder.withParent(myContainingFile);
		builder.withType(CSharpLightTypeDeclarationBuilder.Type.STRUCT);
		builder.addExtendType(new CSharpTypeRefByQName(getProject(), getResolveScope(), DotNetTypes.System.ValueType));

		for(CSharpFieldOrPropertySet set : mySets)
		{
			String name = set.getName();
			if(name == null)
			{
				continue;
			}

			DotNetExpression valueReferenceExpression = set.getValueExpression();

			SetField fieldBuilder = new SetField(getProject(), set);

			if(valueReferenceExpression == null)
			{
				fieldBuilder.withTypeRef(new CSharpTypeRefByQName(getProject(), getResolveScope(), DotNetTypes.System.Object));
			}
			else
			{
				fieldBuilder.withTypeRef(valueReferenceExpression.toTypeRef(true));
			}
			fieldBuilder.addModifier(CSharpModifier.PUBLIC);
			fieldBuilder.withName(name);
			if(set instanceof CSharpNamedFieldOrPropertySet)
			{
				PsiElement nameReferenceExpression = set.getNameElement();

				fieldBuilder.withNameIdentifier(nameReferenceExpression);
				fieldBuilder.setNavigationElement(nameReferenceExpression);
			}
			else if(set instanceof CSharpAnonymFieldOrPropertySet)
			{
				fieldBuilder.setNavigationElement(set.getValueExpression());
			}
			builder.addMember(fieldBuilder);
		}

		return builder;
	}
}
