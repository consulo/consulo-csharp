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

package consulo.csharp.ide.projectView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.BitUtil;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.CSharpElementPresentationUtil;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.ide.DotNetElementPresentationUtil;
import consulo.dotnet.psi.*;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 09.12.13.
 */
public class CSharpElementTreeNode extends CSharpAbstractElementTreeNode<DotNetNamedElement>
{
	public static int getWeight(PsiElement element)
	{
		int defaultWeight = getDefaultWeight(element);
		if(element instanceof DotNetVirtualImplementOwner)
		{
			DotNetTypeRef typeRefForImplement = ((DotNetVirtualImplementOwner) element).getTypeRefForImplement();
			if(typeRefForImplement != DotNetTypeRef.ERROR_TYPE)
			{
				return -defaultWeight;
			}
		}
		return 0;
	}

	private static int getDefaultWeight(PsiElement element)
	{
		if(element instanceof DotNetNamespaceDeclaration)
		{
			return 100;
		}
		else if(element instanceof DotNetTypeDeclaration)
		{
			return 200;
		}
		else if(element instanceof DotNetFieldDeclaration)
		{
			return 300;
		}
		else if(element instanceof DotNetPropertyDeclaration)
		{
			return 400;
		}
		else if(element instanceof DotNetEventDeclaration)
		{
			return 500;
		}
		else if(element instanceof DotNetConstructorDeclaration)
		{
			return 700;
		}
		else if(element instanceof DotNetLikeMethodDeclaration)
		{
			return 600;
		}
		return 0;
	}

	// force expand members for root file nodes (if not than one)
	public static final int FORCE_EXPAND = 1 << 0;
	// if we have solo type - but file name is not equal
	public static final int ALLOW_GRAY_FILE_NAME = 1 << 1;

	private final int myFlags;

	public CSharpElementTreeNode(DotNetNamedElement dotNetMemberOwner, ViewSettings viewSettings, int flags)
	{
		super(dotNetMemberOwner.getProject(), dotNetMemberOwner, viewSettings);
		myFlags = flags;
	}

	@Override
	public int getWeight()
	{
		// namespace
		// type
		// field
		// property
		// event
		// constructor
		// method

		DotNetNamedElement element = getValue();
		int weight = getWeight(element);
		if(weight != 0)
		{
			return weight;
		}
		return super.getWeight();
	}

	@Nullable
	@Override
	@RequiredDispatchThread
	protected Collection<AbstractTreeNode> getChildrenImpl()
	{
		final ViewSettings settings = getSettings();
		if(!settings.isShowMembers() && !BitUtil.isSet(myFlags, FORCE_EXPAND))
		{
			return Collections.emptyList();
		}

		DotNetNamedElement[] members = filterNamespaces(getValue());
		if(members.length == 0)
		{
			return Collections.emptyList();
		}

		List<AbstractTreeNode> list = new ArrayList<>(members.length);
		for(DotNetNamedElement dotNetElement : members)
		{
			list.add(new CSharpElementTreeNode(dotNetElement, settings, 0));
		}
		return list;
	}

	@RequiredReadAction
	private static DotNetNamedElement[] filterNamespaces(DotNetElement declaration)
	{
		if(declaration instanceof DotNetMemberOwner)
		{
			List<DotNetNamedElement> elements = new ArrayList<>();

			DotNetNamedElement[] members = ((DotNetMemberOwner) declaration).getMembers();
			for(DotNetNamedElement member : members)
			{
				if(member instanceof DotNetNamespaceDeclaration)
				{
					member.accept(new CSharpElementVisitor()
					{
						@Override
						@RequiredReadAction
						public void visitElement(PsiElement element)
						{
							ProgressManager.checkCanceled();
							if(element instanceof DotNetMemberOwner)
							{
								for(DotNetNamedElement namedElement : ((DotNetMemberOwner) element).getMembers())
								{
									namedElement.accept(this);
								}
							}
						}

						@Override
						public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
						{
							if(!declaration.isNested())
							{
								elements.add(declaration);
							}
						}

						@Override
						public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
						{
							if(declaration.isDelegate())
							{
								elements.add(declaration);
							}
						}
					});
				}
				else
				{
					elements.add(member);
				}
			}

			return ContainerUtil.toArray(elements, DotNetNamedElement.ARRAY_FACTORY);
		}
		return DotNetNamedElement.EMPTY_ARRAY;
	}

	@Override
	public boolean isAlwaysExpand()
	{
		return super.isAlwaysExpand() || BitUtil.isSet(myFlags, FORCE_EXPAND);
	}

	@Override
	@RequiredDispatchThread
	protected void updateImpl(PresentationData presentationData)
	{
		DotNetNamedElement value = getValue();

		presentationData.setPresentableText(getPresentableText(value));

		if(BitUtil.isSet(myFlags, ALLOW_GRAY_FILE_NAME))
		{
			PsiFile containingFile = value.getContainingFile();
			if(containingFile != null)
			{
				if(!Comparing.equal(FileUtil.getNameWithoutExtension(containingFile.getName()), value.getName()))
				{
					presentationData.setLocationString(containingFile.getName());
				}
			}
		}
	}

	@RequiredReadAction
	public static String getPresentableText(PsiNamedElement value)
	{
		if(value instanceof DotNetLikeMethodDeclaration)
		{
			return CSharpElementPresentationUtil.formatMethod((DotNetLikeMethodDeclaration) value, CSharpElementPresentationUtil.METHOD_SCALA_LIKE_FULL);
		}
		else if(value instanceof DotNetFieldDeclaration)
		{
			return CSharpElementPresentationUtil.formatField((DotNetFieldDeclaration) value);
		}
		else if(value instanceof DotNetNamespaceDeclaration)
		{
			return ((DotNetNamespaceDeclaration) value).getPresentableQName();
		}
		else if(value instanceof DotNetTypeDeclaration)
		{
			return DotNetElementPresentationUtil.formatTypeWithGenericParameters((DotNetTypeDeclaration) value);
		}
		else
		{
			return value.getName();
		}
	}
}
