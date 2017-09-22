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

package consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpAnonymFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpNamedFieldOrPropertySet;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightFieldDeclarationBuilder;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.dotnet.resolve.SimpleTypeResolveResult;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

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
	private CSharpFieldOrPropertySet[] mySets;

	public CSharpAnonymTypeRef(PsiFile containingFile, CSharpFieldOrPropertySet[] sets)
	{
		super(containingFile.getProject());
		myContainingFile = containingFile;
		mySets = sets;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return new SimpleTypeResolveResult(createTypeDeclaration());
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
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

	@NotNull
	@RequiredReadAction
	private DotNetTypeDeclaration createTypeDeclaration()
	{
		CSharpLightTypeDeclarationBuilder builder = new CSharpLightTypeDeclarationBuilder(myContainingFile);
		builder.addModifier(CSharpModifier.PUBLIC);
		builder.withParent(myContainingFile);
		builder.withType(CSharpLightTypeDeclarationBuilder.Type.STRUCT);
		builder.addExtendType(new CSharpTypeRefByQName(myContainingFile, DotNetTypes.System.ValueType));

		for(CSharpFieldOrPropertySet set : mySets)
		{
			String name = set.getName();
			if(name == null)
			{
				continue;
			}

			DotNetExpression valueReferenceExpression = set.getValueExpression();

			SetField fieldBuilder = new SetField(myContainingFile.getProject(), set);

			if(valueReferenceExpression == null)
			{
				fieldBuilder.withTypeRef(new CSharpTypeRefByQName(myContainingFile, DotNetTypes.System.Object));
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
