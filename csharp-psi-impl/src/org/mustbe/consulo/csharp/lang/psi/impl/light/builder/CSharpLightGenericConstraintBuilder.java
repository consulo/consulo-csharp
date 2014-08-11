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

package org.mustbe.consulo.csharp.lang.psi.impl.light.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 11.08.14
 */
public class CSharpLightGenericConstraintBuilder extends CSharpLightElementBuilder<CSharpLightGenericConstraintBuilder> implements
		CSharpGenericConstraint
{
	private final DotNetGenericParameter myParameter;
	private List<CSharpGenericConstraintValue> myConstraintValues = Collections.emptyList();

	public CSharpLightGenericConstraintBuilder(DotNetGenericParameter parameter)
	{
		super(parameter);
		myParameter = parameter;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericConstraint(this);
	}

	@Nullable
	@Override
	public DotNetGenericParameter resolve()
	{
		return myParameter;
	}

	@Nullable
	@Override
	public CSharpReferenceExpression getGenericParameterReference()
	{
		return null;
	}

	@NotNull
	@Override
	public CSharpGenericConstraintValue[] getGenericConstraintValues()
	{
		return ContainerUtil.toArray(myConstraintValues, CSharpGenericConstraintValue.ARRAY_FACTORY);
	}

	public void addTypeConstraint(@NotNull DotNetTypeRef typeRef)
	{
		if(myConstraintValues.isEmpty())
		{
			myConstraintValues = new ArrayList<CSharpGenericConstraintValue>(5);
		}
		myConstraintValues.add(new CSharpLightGenericConstraintTypeValueBuilder(getProject(), typeRef));
	}
}
