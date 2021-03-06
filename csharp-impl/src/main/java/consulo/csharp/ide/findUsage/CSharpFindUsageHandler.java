package consulo.csharp.ide.findUsage;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;

/**
 * @author VISTALL
 * @since 31-Oct-17
 */
public class CSharpFindUsageHandler extends FindUsagesHandler
{
	public CSharpFindUsageHandler(@Nonnull PsiElement psiElement)
	{
		super(psiElement);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public PsiElement[] getPrimaryElements()
	{
		PsiElement psiElement = getPsiElement();
		if(OverrideUtil.isAllowForOverride(psiElement))
		{
			Collection<DotNetVirtualImplementOwner> members = OverrideUtil.collectOverridingMembers((DotNetVirtualImplementOwner) psiElement);
			if(!members.isEmpty())
			{
				MessageDialogBuilder.YesNo builder = MessageDialogBuilder.yesNo("Find Usage", "Search for base target ir current target?");
				builder = builder.yesText("Base Target");
				builder = builder.noText("This Target");

				if(builder.show() == Messages.OK)
				{
					return PsiUtilCore.toPsiElementArray(members);
				}
			}
		}
		return super.getPrimaryElements();
	}

	@Nonnull
	@Override
	public PsiElement[] getSecondaryElements()
	{
		return super.getSecondaryElements();
	}
}
