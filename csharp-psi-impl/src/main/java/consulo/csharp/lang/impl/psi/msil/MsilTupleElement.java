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
package consulo.csharp.lang.impl.psi.msil;

import consulo.csharp.lang.CSharpLanguage;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiNameIdentifierOwner;
import consulo.language.util.IncorrectOperationException;
import org.jspecify.annotations.Nullable;

/**
 * An element of a tuple type of a compiled assembly - named by its TupleElementNamesAttribute, which it navigates to
 *
 * @author VISTALL
 */
public class MsilTupleElement extends LightElement implements PsiNameIdentifierOwner {
    private final PsiElement myAttribute;
    @Nullable
    private final String myName;

    public MsilTupleElement(PsiElement attribute, @Nullable String name) {
        super(attribute.getManager(), CSharpLanguage.INSTANCE);
        myAttribute = attribute;
        myName = name;
    }

    @Nullable
    @Override
    public String getName() {
        return myName;
    }

    @Override
    public PsiElement setName(String name) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return null;
    }

    @Override
    public PsiFile getContainingFile() {
        return myAttribute.getContainingFile();
    }

    @Override
    public PsiElement getNavigationElement() {
        return myAttribute;
    }

    @Override
    public String toString() {
        return "MsilTupleElement:" + myName;
    }
}
