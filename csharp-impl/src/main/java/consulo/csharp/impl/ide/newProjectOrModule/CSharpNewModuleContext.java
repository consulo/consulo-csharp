/*
 * Copyright 2013-2019 consulo.io
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

package consulo.csharp.impl.ide.newProjectOrModule;

import consulo.content.bundle.Sdk;
import consulo.dotnet.DotNetTarget;
import consulo.ide.newModule.NewModuleWizardContextBase;

/**
 * @author VISTALL
 * @since 2019-09-06
 */
public class CSharpNewModuleContext extends NewModuleWizardContextBase
{
	private Sdk mySdk;
	private DotNetTarget myTarget = DotNetTarget.EXECUTABLE;

	public CSharpNewModuleContext(boolean isNewProject)
	{
		super(isNewProject);
	}

	public DotNetTarget getTarget()
	{
		return myTarget;
	}

	public void setTarget(DotNetTarget target)
	{
		myTarget = target;
	}

	public Sdk getSdk()
	{
		return mySdk;
	}

	public void setSdk(Sdk sdk)
	{
		mySdk = sdk;
	}
}
