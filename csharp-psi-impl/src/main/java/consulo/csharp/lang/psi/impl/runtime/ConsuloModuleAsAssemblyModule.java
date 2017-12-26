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

package consulo.csharp.lang.psi.impl.runtime;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Comparing;
import com.intellij.util.ObjectUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.psi.impl.DotNetTypes2;
import consulo.csharp.lang.psi.impl.stub.index.AttributeListIndex;
import consulo.dotnet.module.DotNetAssemblyUtil;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;

/**
 * @author VISTALL
 * @since 14-Jun-17
 */
class ConsuloModuleAsAssemblyModule implements AssemblyModule
{
	private final Module myModule;

	ConsuloModuleAsAssemblyModule(Module module)
	{
		myModule = module;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String getName()
	{
		String assemblyTitle = DotNetAssemblyUtil.getAssemblyTitle(myModule);
		return ObjectUtil.notNull(assemblyTitle, myModule.getName());
	}

	@RequiredReadAction
	@Override
	public boolean isAllowedAssembly(@NotNull String assemblyName)
	{
		Collection<CSharpAttributeList> attributeLists = AttributeListIndex.getInstance().get(DotNetAttributeTargetType.ASSEMBLY, myModule.getProject(), myModule.getModuleScope());

		for(CSharpAttributeList attributeList : attributeLists)
		{
			for(CSharpAttribute attribute : attributeList.getAttributes())
			{
				DotNetTypeDeclaration dotNetTypeDeclaration = attribute.resolveToType();
				if(dotNetTypeDeclaration == null)
				{
					continue;
				}

				if(DotNetTypes2.System.Runtime.CompilerServices.InternalsVisibleToAttribute.equalsIgnoreCase(dotNetTypeDeclaration.getVmQName()))
				{
					Module attributeModule = ModuleUtilCore.findModuleForPsiElement(attribute);
					if(attributeModule == null || !attributeModule.equals(myModule))
					{
						continue;
					}

					DotNetExpression[] parameterExpressions = attribute.getParameterExpressions();
					if(parameterExpressions.length == 0)
					{
						continue;
					}
					String valueAs = new ConstantExpressionEvaluator(parameterExpressions[0]).getValueAs(String.class);
					if(Comparing.equal(valueAs, assemblyName))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean equals(@NotNull AssemblyModule module)
	{
		return module instanceof ConsuloModuleAsAssemblyModule && myModule.equals(((ConsuloModuleAsAssemblyModule) module).myModule);
	}
}
