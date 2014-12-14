package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.ide.highlight.quickFix.ReplaceTypeQuickFix;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.cache.Quaternary;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 14.12.14
 */
public class CS0030 extends CompilerCheck<PsiElement>
{
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull PsiElement element)
	{
		Quaternary<DotNetTypeRef, DotNetTypeRef, ? extends PsiElement, IntentionAction[]> resolve = resolve(element);
		if(resolve == null)
		{
			return null;
		}

		DotNetTypeRef firstTypeRef = resolve.getFirst();
		if(firstTypeRef == DotNetTypeRef.AUTO_TYPE)
		{
			return null;
		}

		DotNetTypeRef secondTypeRef = resolve.getSecond();
		PsiElement elementToHighlight = resolve.getThird();

		CSharpTypeUtil.InheritResult inheritResult = CSharpTypeUtil.isInheritable(firstTypeRef, secondTypeRef, element, null);
		if(!inheritResult.isSuccess())
		{
			CompilerCheckBuilder builder = newBuilder(elementToHighlight, CSharpTypeRefPresentationUtil.buildText(secondTypeRef, element,
					CS0029.TYPE_FLAGS), CSharpTypeRefPresentationUtil.buildText(firstTypeRef, element, CS0029.TYPE_FLAGS));

			IntentionAction[] four = resolve.getFour();
			if(four != null)
			{
				for(IntentionAction intentionAction : four)
				{
					builder.addQuickFix(intentionAction);
				}
			}
			return builder;
		}

		return null;
	}

	@Nullable
	private Quaternary<DotNetTypeRef, DotNetTypeRef, ? extends PsiElement, IntentionAction[]> resolve(PsiElement element)
	{
		if(element instanceof CSharpForeachStatementImpl)
		{
			CSharpLocalVariable variable = ((CSharpForeachStatementImpl) element).getVariable();
			if(variable == null)
			{
				return null;
			}

			DotNetType type = variable.getType();
			if(type == null)
			{
				return null;
			}
			DotNetTypeRef variableTypeRef = type.toTypeRef();
			if(variableTypeRef == DotNetTypeRef.AUTO_TYPE || variableTypeRef == DotNetTypeRef.ERROR_TYPE)
			{
				return null;
			}
			DotNetTypeRef iterableTypeRef = CSharpResolveUtil.resolveIterableType((CSharpForeachStatementImpl) element);
			if(iterableTypeRef == DotNetTypeRef.ERROR_TYPE)
			{
				return null;
			}

			return Quaternary.create(variableTypeRef, iterableTypeRef, type, new IntentionAction[]{
					new ReplaceTypeQuickFix(type, DotNetTypeRef.AUTO_TYPE),
					new ReplaceTypeQuickFix(type, iterableTypeRef)
			});
		}
		return null;
	}
}
