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

package consulo.csharp.impl.ide.codeInsight.actions;

import java.util.List;

import jakarta.annotation.Nonnull;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.impl.ide.liveTemplates.expression.TypeRefExpression;
import consulo.csharp.lang.impl.psi.CSharpContextUtil;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.component.util.localize.BundleBase;
import consulo.language.psi.PsiFile;
import consulo.language.editor.template.Template;

/**
 * @author VISTALL
 * @since 06.01.15
 */
public class CreateUnresolvedPropertyFix extends CreateUnresolvedFieldFix
{
	public CreateUnresolvedPropertyFix(CSharpReferenceExpression expression)
	{
		super(expression);
	}

	@Nonnull
	@Override
	public String getText()
	{
		return BundleBase.format("Create property ''{0}''", myReferenceName);
	}

	@RequiredReadAction
	@Override
	public void buildTemplate(@Nonnull CreateUnresolvedElementFixContext context,
			CSharpContextUtil.ContextType contextType,
			@Nonnull PsiFile file,
			@Nonnull Template template)
	{
		template.addTextSegment("public ");

		if(contextType == CSharpContextUtil.ContextType.STATIC)
		{
			template.addTextSegment("static ");
		}

		// get expected from method call expression not reference
		List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(context.getExpression());

		if(!expectedTypeRefs.isEmpty())
		{
			template.addVariable(new TypeRefExpression(expectedTypeRefs, file), true);
		}
		else
		{
			template.addVariable(new TypeRefExpression(new CSharpTypeRefByQName(file, DotNetTypes.System.Object), file), true);
		}

		template.addTextSegment(" ");
		template.addTextSegment(myReferenceName);
		template.addTextSegment("\n{\n");
		template.addTextSegment("get;set;\n");
		template.addTextSegment("}");
		template.addEndVariable();
	}
}
