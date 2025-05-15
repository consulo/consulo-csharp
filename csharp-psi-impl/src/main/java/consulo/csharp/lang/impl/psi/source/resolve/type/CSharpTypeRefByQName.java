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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.msil.CSharpTransform;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.DumbService;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpTypeRefByQName extends DotNetTypeRefWithCachedResult implements CSharpLikeTypeRef {
    @Nonnull
    private final String myQualifiedName;

    public CSharpTypeRefByQName(@Nonnull Project project, @Nonnull GlobalSearchScope searchScope, @Nonnull String qualifiedName) {
        super(project, searchScope);
        myQualifiedName = qualifiedName;
    }

    @RequiredReadAction
    @Deprecated
    public CSharpTypeRefByQName(@Nonnull PsiElement scope, @Nonnull String qualifiedName) {
        this(scope.getProject(), scope.getResolveScope(), qualifiedName);
    }

    @Nonnull
    @Override
    public String getVmQName() {
        return myQualifiedName;
    }

    @RequiredReadAction
    @Nonnull
    @Override
    protected DotNetTypeResolveResult resolveResult() {
        if (DumbService.isDumb(getProject())) {
            return DotNetTypeResolveResult.EMPTY;
        }

        DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(getProject()).findType(myQualifiedName, myResolveScope, CSharpTransform.INSTANCE);

        if (type == null) {
            return DotNetTypeResolveResult.EMPTY;
        }

        return new CSharpUserTypeRef.Result<>(type, DotNetGenericExtractor.EMPTY);
    }

    @Override
    public boolean isEqualToVmQName(@Nonnull String vmQName) {
        return vmQName.equals(myQualifiedName);
    }

    @RequiredReadAction
    @Nonnull
    @Override
    public String toString() {
        return myQualifiedName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Delegate) {
            obj = ((Delegate) obj).getDelegate();
        }
        return CSharpLikeTypeRef.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return CSharpLikeTypeRef.hashCode(this);
    }

    @Nonnull
    @Override
    public DotNetTypeRef getInnerTypeRef() {
        return new CSharpTypeRefByQName(myProject, myResolveScope, myQualifiedName);
    }

    @Override
    public DotNetGenericExtractor getExtractor() {
        return DotNetGenericExtractor.EMPTY;
    }
}
