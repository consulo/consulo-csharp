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

package org.mustbe.consulo.csharp.ide.msil.representation.builder;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.dll.vfs.builder.block.LineStubBlock;
import org.mustbe.consulo.dotnet.dll.vfs.builder.block.StubBlock;
import org.mustbe.consulo.dotnet.dll.vfs.builder.block.StubBlockUtil;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.psi.PsiElement;
import com.intellij.util.PairFunction;

/**
 * @author VISTALL
 * @since 02.06.14
 */
public class CSharpStubBuilderVisitor extends CSharpElementVisitor
{
	@NotNull
	public static List<StubBlock> buildBlocks(PsiElement qualifiedElement)
	{
		CSharpStubBuilderVisitor visitor = new CSharpStubBuilderVisitor();
		qualifiedElement.accept(visitor);
		return visitor.getBlocks();
	}

	private List<StubBlock> myBlocks = new ArrayList<StubBlock>(2);

	@Override
	public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processModifierList(builder, declaration);
		builder.append(declaration.toTypeRef(false).getQualifiedText());
		builder.append(" ");
		builder.append(declaration.getName());
		myBlocks.add(new StubBlock(builder, null, StubBlock.BRACES));
	}

	@Override
	public void visitFieldDeclaration(DotNetFieldDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processModifierList(builder, declaration);
		builder.append(declaration.toTypeRef(false).getQualifiedText());
		builder.append(" ");
		builder.append(declaration.getName());
		builder.append(";\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	public void visitEventDeclaration(CSharpEventDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processModifierList(builder, declaration);
		builder.append("event ");
		builder.append(declaration.toTypeRef(false).getQualifiedText());
		builder.append(" ");
		builder.append(declaration.getName());
		myBlocks.add(new StubBlock(builder, null, StubBlock.BRACES));
	}

	@Override
	public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		processModifierList(builder, declaration);

		if(declaration.isDelegate())
		{
			builder.append("delegate ");
		}
		builder.append(declaration.getReturnTypeRef().getQualifiedText());
		builder.append(" ");
		if(declaration.isOperator())
		{
			builder.append("operator ");
		}
		builder.append(declaration.getName());
		builder.append("(");
		StubBlockUtil.join(builder, declaration.getParameters(), new PairFunction<StringBuilder, DotNetParameter, Void>()
		{
			@Nullable
			@Override
			public Void fun(StringBuilder t, DotNetParameter v)
			{
				processModifierList(t, v);
				t.append(v.toTypeRef(false).getQualifiedText());
				t.append(" ");
				t.append(v.getName());
				return null;
			}
		}, ", ");
		builder.append(")");

		boolean canHaveBody = !declaration.hasModifier(CSharpModifier.ABSTRACT);

		if(canHaveBody)
		{
			builder.append(" { /* compiled code */ }\n");
		}
		else
		{
			builder.append(";\n");
		}
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processModifierList(builder, declaration);

		builder.append("class ");
		builder.append(declaration.getName());

		StubBlock e = new StubBlock(builder, null, StubBlock.BRACES);
		myBlocks.add(e);

		for(DotNetNamedElement dotNetNamedElement : declaration.getMembers())
		{
			e.getBlocks().addAll(buildBlocks(dotNetNamedElement));
		}
	}

	private static void processModifierList(StringBuilder builder, DotNetModifierListOwner owner)
	{
		DotNetModifierList modifierList = owner.getModifierList();
		if(modifierList != null)
		{
			for(DotNetModifier dotNetModifier : modifierList.getModifiers())
			{
				builder.append(dotNetModifier.getPresentableText()).append(" ");
			}
		}
	}

	public List<StubBlock> getBlocks()
	{
		return myBlocks;
	}
}
