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

package org.mustbe.consulo.csharp.ide.refactoring.introduceVariable;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.RefactoringBundle;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class CSharpIntroduceLocalVariableHandler extends CSharpIntroduceHandler
{
	public CSharpIntroduceLocalVariableHandler()
	{
		super(RefactoringBundle.message("introduce.variable.title"));
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected Collection<String> getSuggestedNames(@NotNull DotNetExpression initializer)
	{
		Collection<String> suggestedNames = super.getSuggestedNames(initializer);
		if(initializer instanceof CSharpMethodCallExpressionImpl)
		{
			DotNetExpression callExpression = ((CSharpMethodCallExpressionImpl) initializer).getCallExpression();
			if(callExpression instanceof CSharpReferenceExpression && ((CSharpReferenceExpression) callExpression).getQualifier() == null)
			{
				removeCollisionOnNonQualifiedReferenceExpressions(suggestedNames, (CSharpReferenceExpression) callExpression);
			}
		}
		else if(initializer instanceof CSharpReferenceExpression && ((CSharpReferenceExpression) initializer).getQualifier() == null)
		{
			removeCollisionOnNonQualifiedReferenceExpressions(suggestedNames, (CSharpReferenceExpression) initializer);
		}
		return suggestedNames;
	}

	private void removeCollisionOnNonQualifiedReferenceExpressions(Collection<String> suggestedNames, CSharpReferenceExpression referenceExpression)
	{
		String referenceName = referenceExpression.getReferenceName();
		suggestedNames.remove(referenceName);

		int index = 1;
		String lastName = null;
		while(suggestedNames.contains(lastName = (referenceName + index)))
		{
			index ++;
		}
		suggestedNames.add(lastName);
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected String getDeclarationString(CSharpIntroduceOperation operation, String initExpression)
	{
		StringBuilder builder = new StringBuilder();
		CSharpCodeGenerationSettings generationSettings = CSharpCodeGenerationSettings.getInstance(operation.getProject());
		DotNetExpression initializer = operation.getInitializer();
		if(generationSettings.USE_VAR_FOR_EXTRACT_LOCAL_VARIABLE)
		{
			builder.append("var");
		}
		else
		{
			DotNetTypeRef typeRef = initializer.toTypeRef(true);
			if(typeRef == DotNetTypeRef.AUTO_TYPE || typeRef == DotNetTypeRef.ERROR_TYPE || typeRef == DotNetTypeRef.UNKNOWN_TYPE)
			{
				builder.append(StringUtil.getShortName(DotNetTypes.System.Object));
			}
			else
			{
				CSharpTypeRefPresentationUtil.appendTypeRef(initializer, builder, typeRef, CSharpTypeRefPresentationUtil.TYPE_KEYWORD);
			}
		}
		builder.append(" ").append(operation.getName()).append(" = ").append(initExpression);
		PsiElement parent = initializer.getParent();
		if(!(parent instanceof CSharpExpressionStatementImpl) || ((CSharpExpressionStatementImpl) parent).getExpression() != initializer)
		{
			builder.append(";");
		}
		builder.append('\n');
		return builder.toString();
	}
}
