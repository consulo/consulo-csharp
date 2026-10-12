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
package consulo.csharp.impl.ide.presentation;

import consulo.annotation.access.RequiredReadAction;
import consulo.component.util.Iconable;
import consulo.csharp.impl.ide.CSharpElementPresentationUtil;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.dotnet.psi.*;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.navigation.ItemPresentationProvider;
import consulo.navigation.NavigationItem;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2013-12-21
 */
public abstract class CSharpElementPresentationProviderBase<T extends DotNetQualifiedElement & NavigationItem> implements ItemPresentationProvider<T> {
    public static class ItemPresentationImpl implements ItemPresentation {
        private final DotNetQualifiedElement myDeclaration;

        public ItemPresentationImpl(DotNetQualifiedElement declaration) {
            myDeclaration = declaration;
        }

        @Override
        @RequiredReadAction
        public LocalizeValue getPresentableText() {
            if (myDeclaration instanceof DotNetTypeDeclaration typeDecl) {
                return LocalizeValue.of(DotNetElementPresentationUtil.formatTypeWithGenericParameters(typeDecl));
            }
            else if (myDeclaration instanceof DotNetMethodDeclaration dotNetMethodDecl) {
                if (myDeclaration instanceof CSharpMethodDeclaration methodDecl && methodDecl.isDelegate()) {
                    return LocalizeValue.of(DotNetElementPresentationUtil.formatTypeWithGenericParameters(methodDecl));
                }
                return LocalizeValue.of(CSharpElementPresentationUtil.formatMethod(dotNetMethodDecl, 0));
            }
            else if (myDeclaration instanceof CSharpConstructorDeclaration constructorDecl) {
                return LocalizeValue.of(CSharpElementPresentationUtil.formatMethod(constructorDecl, 0));
            }
            else if (myDeclaration instanceof CSharpIndexMethodDeclaration indexMethodDecl) {
                return LocalizeValue.of(CSharpElementPresentationUtil.formatMethod(indexMethodDecl, 0));
            }
            return LocalizeValue.ofNullable(myDeclaration.getName());
        }

        @Nullable
        @Override
        @RequiredUIAccess
        public String getLocationString() {
            String presentableParentQName = myDeclaration.getPresentableParentQName();
            if (StringUtil.isEmpty(presentableParentQName)) {
                return null;
            }
            return "(" + presentableParentQName + ")";
        }

        @Nullable
        @Override
        @RequiredReadAction
        public Image getIcon() {
            return IconDescriptorUpdaters.getIcon(myDeclaration, Iconable.ICON_FLAG_VISIBILITY);
        }
    }

    @Override
    public ItemPresentation getPresentation(T qualifiedElement) {
        return new ItemPresentationImpl(qualifiedElement);
    }
}
