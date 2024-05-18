package consulo.csharp.impl.ide.findUsage;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.find.FindUsagesHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.ui.ex.awt.MessageDialogBuilder;
import consulo.ui.ex.awt.Messages;

import javax.annotation.Nonnull;
import java.util.Collection;

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
