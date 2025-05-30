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

package consulo.csharp.lang.impl.psi.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpGenericConstraintUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.List;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpGenericParameterResolveContext extends CSharpBaseResolveContext<DotNetGenericParameter> {
    @RequiredReadAction
    public CSharpGenericParameterResolveContext(@Nonnull DotNetGenericParameter element) {
        super(element, DotNetGenericExtractor.EMPTY, null);
    }

    @Override
    public void acceptChildren(CSharpElementVisitor visitor) {

    }

    @RequiredReadAction
    @Nonnull
    @Override
    protected List<DotNetTypeRef> getExtendTypeRefs() {
        return Arrays.asList(CSharpGenericConstraintUtil.getExtendTypes(myElement));
    }
}
