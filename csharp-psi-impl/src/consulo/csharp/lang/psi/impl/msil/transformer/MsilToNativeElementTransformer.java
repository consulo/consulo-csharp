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

package consulo.csharp.lang.psi.impl.msil.transformer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.ToNativeElementTransformer;
import consulo.csharp.lang.psi.impl.msil.MsilMethodAsCSharpMethodDeclaration;
import consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.msil.lang.psi.MsilClassEntry;
import consulo.msil.lang.psi.MsilFile;

/**
 * @author VISTALL
 * @since 15.03.2016
 */
public class MsilToNativeElementTransformer implements ToNativeElementTransformer
{
	private static final Logger LOGGER = Logger.getInstance(MsilToNativeElementTransformer.class);

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement transform(@NotNull PsiElement element)
	{
		if(element instanceof MsilClassEntry)
		{
			if(((MsilClassEntry) element).isNested())
			{
				MsilClassEntry rootClassEntry = findRootClassEntry((MsilClassEntry) element);

				PsiElement wrappedElement = MsilToCSharpUtil.wrap(rootClassEntry, null);
				// we wrap it
				if(wrappedElement != rootClassEntry)
				{
					PsiElement elementByOriginal = findElementByOriginal(wrappedElement, element);
					if(elementByOriginal != null)
					{
						return elementByOriginal;
					}
					else
					{
						LOGGER.warn("We cant find by original element class: " + rootClassEntry.getVmQName() + "/" + ((MsilClassEntry) element).getVmQName());
					}
				}
			}
			else
			{
				return MsilToCSharpUtil.wrap(element, null);
			}
		}
		return null;
	}

	@NotNull
	public static MsilClassEntry findRootClassEntry(@NotNull MsilClassEntry element)
	{
		PsiFile containingFile = element.getContainingFile();
		if(!(containingFile instanceof MsilFile))
		{
			return element;
		}

		MsilClassEntry originalClassEntry = null;

		PsiElement temp = element;
		while(!(temp instanceof MsilFile))
		{
			if(temp instanceof MsilClassEntry && temp.getParent() instanceof MsilFile)
			{
				originalClassEntry = (MsilClassEntry) temp;
				break;
			}

			temp = temp.getParent();
		}

		if(originalClassEntry == null)
		{
			throw new IllegalArgumentException("Cant determinate parent msil class for: " + containingFile.getName());
		}

		return originalClassEntry;
	}


	@Nullable
	public static PsiElement findElementByOriginal(@NotNull PsiElement wrappedElement, @NotNull PsiElement originalTarget)
	{
		PsiElement originalElement = wrappedElement.getOriginalElement();

		if(originalElement.isEquivalentTo(originalTarget))
		{
			return wrappedElement;
		}

		if(wrappedElement instanceof MsilMethodAsCSharpMethodDeclaration)
		{
			MsilClassEntry delegate = ((MsilMethodAsCSharpMethodDeclaration) wrappedElement).getDelegate();
			if(delegate != null && delegate.isEquivalentTo(originalTarget))
			{
				return wrappedElement;
			}
		}

		if(wrappedElement instanceof DotNetMemberOwner)
		{
			DotNetNamedElement[] members = ((DotNetMemberOwner) wrappedElement).getMembers();
			for(DotNetNamedElement member : members)
			{
				PsiElement elementByOriginal = findElementByOriginal(member, originalTarget);
				if(elementByOriginal != null)
				{
					return elementByOriginal;
				}
			}
		}
		return null;
	}
}
