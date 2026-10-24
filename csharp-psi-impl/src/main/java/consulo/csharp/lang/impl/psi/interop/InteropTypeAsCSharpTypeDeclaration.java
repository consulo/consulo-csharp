/*
 * Copyright 2013-2026 consulo.io
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
package consulo.csharp.lang.impl.psi.interop;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightTypeDeclarationBuilder;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import org.jspecify.annotations.Nullable;

/**
 * A type of another .NET language as a C# type: names, modifiers, base types and members are taken from the
 * {@link DotNetTypeDeclaration}; navigation goes to the original.
 *
 * @author VISTALL
 * @since 2026-10-25
 */
public class InteropTypeAsCSharpTypeDeclaration extends CSharpLightTypeDeclarationBuilder {
    private final DotNetTypeDeclaration myOriginal;

    @RequiredReadAction
    public InteropTypeAsCSharpTypeDeclaration(DotNetTypeDeclaration original, GlobalSearchScope scope) {
        super(original.getProject(), scope);
        myOriginal = original;
        setNavigationElement(original);
        withHashAndEqualObject(original);
    }

    public DotNetTypeDeclaration getOriginal() {
        return myOriginal;
    }

    @Override
    public PsiElement getOriginalElement() {
        return myOriginal;
    }

    @RequiredReadAction
    @Override
    public String getVmQName() {
        String vmQName = myOriginal.getVmQName();
        return vmQName == null ? super.getVmQName() : vmQName;
    }

    @RequiredReadAction
    @Override
    public @Nullable String getVmName() {
        String vmName = myOriginal.getVmName();
        return vmName == null ? super.getVmName() : vmName;
    }

    @RequiredReadAction
    @Override
    public int getGenericParametersCount() {
        return myOriginal.getGenericParametersCount();
    }

    @RequiredReadAction
    @Override
    public int getTextOffset() {
        return myOriginal.getTextOffset();
    }

    @Override
    public boolean isValid() {
        return myOriginal.isValid();
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        if (another == this || another == myOriginal) {
            return true;
        }
        if (another instanceof InteropTypeAsCSharpTypeDeclaration other) {
            return other.myOriginal.equals(myOriginal);
        }
        return super.isEquivalentTo(another);
    }

    @Override
    public String toString() {
        return "InteropTypeAsCSharpTypeDeclaration:" + myOriginal;
    }
}
