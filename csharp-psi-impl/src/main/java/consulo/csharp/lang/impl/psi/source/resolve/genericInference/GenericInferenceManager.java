/*
 * Copyright 2013-2018 consulo.io
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

package consulo.csharp.lang.impl.psi.source.resolve.genericInference;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.util.SimpleModificationTracker;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;

import java.util.function.Function;

/**
 * @author VISTALL
 * @since 2018-07-23
 */
@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class GenericInferenceManager extends SimpleModificationTracker
{
	@Nonnull
	public static GenericInferenceManager getInstance(@Nonnull Project project)
	{
		return project.getInstance(GenericInferenceManager.class);
	}

	public final ThreadLocal<InferenceSessionData> myInsideInferenceSession = new ThreadLocal<>();

	public boolean isInsideGenericInferenceSession()
	{
		return myInsideInferenceSession.get() != null;
	}

	public boolean isInsideGenericInferenceSession(@Nullable PsiElement element)
	{
		if(element == null)
		{
			return false;
		}
		InferenceSessionData inferenceSessionData = myInsideInferenceSession.get();
		return inferenceSessionData != null && inferenceSessionData.myData.containsKey(element);
	}

	@Nullable
	public DotNetTypeRef getInferenceSessionTypeRef(@Nonnull CSharpLambdaExpressionImpl expression)
	{
		InferenceSessionData inferenceSessionData = myInsideInferenceSession.get();
		return inferenceSessionData != null ? inferenceSessionData.getTypeRef(expression) : null;
	}

	@Nonnull
	public <R> R doWithSession(@Nonnull CSharpLambdaExpressionImpl lambdaExpression, @Nonnull Function<InferenceSessionData, R> dataConsumer)
	{
		try
		{
			InferenceSessionData inferenceSessionData = myInsideInferenceSession.get();
			if(inferenceSessionData == null)
			{
				myInsideInferenceSession.set(inferenceSessionData = new InferenceSessionData());
			}

			incModificationCount();

			return dataConsumer.apply(inferenceSessionData);
		}
		finally
		{
			incModificationCount();

			if(myInsideInferenceSession.get().finish(lambdaExpression))
			{
				myInsideInferenceSession.set(null);
			}
		}
	}
}
