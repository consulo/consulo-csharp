package consulo.csharp.ide.codeInsight.hits;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.codeInsight.hints.MethodInfo;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpConstantUtil;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpExpressionWithOperatorImpl;
import consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolver;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NErrorCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NNamedCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NParamsCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

/**
 * @author VISTALL
 * @since 16-Jan-17
 */
public class CSharpParameterHintsProvider implements InlayParameterHintsProvider
{
	private static String[] ourDefaultBlacklist = {
			"*.Write(*)",
			"*.WriteLine(*)",
			"System.Func*",
			"System.Action*",
			"*.Add(value)",
			"*.Add(item)",
			"*.Add(key, value)",
			"*.TryParse(*, *)"
	};

	@NotNull
	@Override
	@RequiredReadAction
	public List<InlayInfo> getParameterHints(@NotNull PsiElement psiElement)
	{
		if(psiElement instanceof CSharpExpressionWithOperatorImpl || psiElement instanceof CSharpOperatorReferenceImpl)
		{
			return Collections.emptyList();
		}

		if(psiElement instanceof CSharpCallArgumentListOwner)
		{
			PsiElement callable = ((CSharpCallArgumentListOwner) psiElement).resolveToCallable();
			if(!(callable instanceof CSharpMethodDeclaration) && !(callable instanceof CSharpConstructorDeclaration) && !(callable instanceof DotNetVariable))
			{
				return Collections.emptyList();
			}

			List<InlayInfo> list = new SmartList<>();
			CSharpCallArgument[] callArguments = ((CSharpCallArgumentListOwner) psiElement).getCallArguments();

			List<NCallArgument> argumentList = buildCallArguments(callArguments, callable, callable);

			for(NCallArgument nCallArgument : argumentList)
			{
				if(nCallArgument instanceof NNamedCallArgument || nCallArgument instanceof NErrorCallArgument || nCallArgument instanceof NParamsCallArgument)
				{
					continue;
				}

				PsiElement parameterElement = nCallArgument.getParameterElement();
				DotNetExpression argumentExpression = nCallArgument.getCallArgument() == null ? null : nCallArgument.getCallArgument().getArgumentExpression();
				if(!CSharpConstantUtil.isConstant(argumentExpression) || parameterElement == null)
				{
					continue;
				}

				String parameterName = nCallArgument.getParameterName();
				if(parameterName == null)
				{
					continue;
				}

				list.add(new InlayInfo(parameterName, argumentExpression.getTextOffset()));
			}
			return list;
		}
		return Collections.emptyList();
	}

	@RequiredReadAction
	private static List<NCallArgument> buildCallArguments(@NotNull CSharpCallArgument[] callArguments, @NotNull PsiElement callable, @NotNull PsiElement scope)
	{
		if(callable instanceof DotNetVariable)
		{
			DotNetTypeRef ref = ((DotNetVariable) callable).toTypeRef(true);
			DotNetTypeResolveResult resolve = ref.resolve();
			if(resolve instanceof CSharpLambdaResolveResult)
			{
				return MethodResolver.buildCallArguments(callArguments, ((CSharpLambdaResolveResult) resolve).getParameterInfos(), scope);
			}
		}
		else if(callable instanceof DotNetParameterListOwner)
		{
			return MethodResolver.buildCallArguments(callArguments, (DotNetParameterListOwner) callable, scope);
		}

		return Collections.emptyList();
	}

	@Nullable
	@Override
	@RequiredReadAction
	public MethodInfo getMethodInfo(@NotNull PsiElement call)
	{
		PsiElement callable;
		if(call instanceof CSharpCallArgumentListOwner)
		{
			callable = ((CSharpCallArgumentListOwner) call).resolveToCallable();
			if(!(callable instanceof CSharpMethodDeclaration) && !(callable instanceof DotNetVariable) && !(callable instanceof CSharpConstructorDeclaration))
			{
				return null;
			}
		}
		else
		{
			return null;
		}

		String name = null;
		CSharpSimpleParameterInfo[] params = CSharpSimpleParameterInfo.EMPTY_ARRAY;
		if(callable instanceof DotNetVariable)
		{
			DotNetTypeRef ref = ((DotNetVariable) callable).toTypeRef(true);
			DotNetTypeResolveResult resolve = ref.resolve();
			if(resolve instanceof CSharpLambdaResolveResult)
			{
				name = ((CSharpLambdaResolveResult) resolve).getTarget().getPresentableQName();
				params = ((CSharpLambdaResolveResult) resolve).getParameterInfos();
			}
			else
			{
				return null;
			}
		}
		else if(callable instanceof CSharpMethodDeclaration || callable instanceof CSharpConstructorDeclaration)
		{
			params = ((CSharpSimpleLikeMethodAsElement) callable).getParameterInfos();

			CSharpTypeDeclaration declaration = PsiTreeUtil.getParentOfType(callable, CSharpTypeDeclaration.class);
			if(declaration != null)
			{
				name = declaration.getPresentableQName() + ".";
			}
			name += ((PsiNamedElement) callable).getName();
		}

		List<String> paramNames = ContainerUtil.map(params, it -> it.getNotNullName());
		return new MethodInfo(name, paramNames);
	}

	@NotNull
	@Override
	public Set<String> getDefaultBlackList()
	{
		return ContainerUtil.newHashSet(ourDefaultBlacklist);
	}
}
