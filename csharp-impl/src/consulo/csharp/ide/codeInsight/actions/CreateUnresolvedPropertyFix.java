/*
 * Copyright 2013-2015 must-be.org
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

package consulo.csharp.ide.codeInsight.actions;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.ide.liveTemplates.expression.TypeRefExpression;
import consulo.csharp.lang.psi.CSharpContextUtil;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import com.intellij.BundleBase;
import com.intellij.codeInsight.template.Template;
import com.intellij.psi.PsiFile;

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

	@NotNull
	@Override
	public String getText()
	{
		return BundleBase.format("Create property ''{0}''", myReferenceName);
	}

	@RequiredReadAction
	@Override
	public void buildTemplate(@NotNull CreateUnresolvedElementFixContext context,
			CSharpContextUtil.ContextType contextType,
			@NotNull PsiFile file,
			@NotNull Template template)
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
