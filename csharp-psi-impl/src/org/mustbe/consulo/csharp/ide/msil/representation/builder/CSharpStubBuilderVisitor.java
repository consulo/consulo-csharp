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
import org.mustbe.consulo.csharp.lang.lexer.CSharpLexer;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.dotnet.dll.vfs.builder.block.LineStubBlock;
import org.mustbe.consulo.dotnet.dll.vfs.builder.block.StubBlock;
import org.mustbe.consulo.dotnet.dll.vfs.builder.block.StubBlockUtil;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
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
		appendTypeRef(builder, declaration.toTypeRef(false));
		builder.append(" ");
		appendName(declaration, builder);
		myBlocks.add(new StubBlock(builder, null, StubBlock.BRACES));
	}

	@Override
	public void visitEnumConstantDeclaration(CSharpEnumConstantDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(declaration.getName());
		builder.append(",\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	public void visitFieldDeclaration(DotNetFieldDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processModifierList(builder, declaration);
		appendTypeRef(builder, declaration.toTypeRef(false));
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
		appendTypeRef(builder, declaration.toTypeRef(false));
		builder.append(" ");
		appendName(declaration, builder);
		myBlocks.add(new StubBlock(builder, null, StubBlock.BRACES));
	}

	@Override
	public void visitArrayMethodDeclaration(CSharpArrayMethodDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		processModifierList(builder, declaration);
		appendTypeRef(builder, declaration.getReturnTypeRef());
		builder.append(" ");
		appendName(declaration, builder);
		processParameterList(declaration, builder, '[', ']');
		builder.append(" { /* compiled code */ }\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	public void visitConversionMethodDeclaration(CSharpConversionMethodDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		processModifierList(builder, declaration);
		appendTypeRef(builder, declaration.getReturnTypeRef());
		builder.append(" ");
		builder.append("operator ");
		appendTypeRef(builder, declaration.getConversionTypeRef());
		processParameterList(declaration, builder, '(', ')');
		builder.append(" { /* compiled code */ }\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		processModifierList(builder, declaration);
		builder.append(declaration.getName());
		processParameterList(declaration, builder, '(', ')');
		builder.append(" { /* compiled code */ }\n");
		myBlocks.add(new LineStubBlock(builder));
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
		appendTypeRef(builder, declaration.getReturnTypeRef());
		builder.append(" ");
		if(declaration.isOperator())
		{
			builder.append("operator ");
		}
		appendName(declaration, builder);
		processGenericParameterList(builder, declaration);
		processParameterList(declaration, builder, '(', ')');

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

		if(declaration.isEnum())
		{
			builder.append("enum ");
		}
		else if(declaration.isStruct())
		{
			builder.append("struct ");
		}
		else
		{
			builder.append("class ");
		}

		builder.append(declaration.getName());
		processGenericParameterList(builder, declaration);

		if(declaration.isEnum())
		{
			DotNetTypeRef typeRefForEnumConstants = declaration.getTypeRefForEnumConstants();
			builder.append(" : ");
			appendTypeRef(builder, typeRefForEnumConstants);
		}
		else
		{
			DotNetTypeRef[] extendTypeRefs = declaration.getExtendTypeRefs();
			if(extendTypeRefs.length > 0)
			{
				builder.append(" : ");
				StubBlockUtil.join(builder, extendTypeRefs, new PairFunction<StringBuilder, DotNetTypeRef, Void>()
				{
					@Nullable
					@Override
					public Void fun(StringBuilder builder, DotNetTypeRef typeRef)
					{
						appendTypeRef(builder, typeRef);
						return null;
					}
				}, ", ");
			}
		}
		StubBlock e = new StubBlock(builder, null, StubBlock.BRACES);
		myBlocks.add(e);

		for(DotNetNamedElement dotNetNamedElement : declaration.getMembers())
		{
			e.getBlocks().addAll(buildBlocks(dotNetNamedElement));
		}
	}

	private static void appendTypeRef(@NotNull StringBuilder builder, @NotNull DotNetTypeRef typeRef)
	{
		if(typeRef instanceof CSharpNativeTypeRef)
		{
			builder.append(typeRef.getPresentableText());
		}
		else if(typeRef instanceof CSharpArrayTypeRef)
		{
			appendTypeRef(builder, ((CSharpArrayTypeRef) typeRef).getInnerTypeRef());
			builder.append("[]");
		}
		else if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			appendTypeRef(builder, ((DotNetGenericWrapperTypeRef) typeRef).getInnerTypeRef());
			DotNetTypeRef[] argumentTypeRefs = ((DotNetGenericWrapperTypeRef) typeRef).getArgumentTypeRefs();
			builder.append("<");
			StubBlockUtil.join(builder, argumentTypeRefs, new PairFunction<StringBuilder, DotNetTypeRef, Void>()
			{
				@Nullable
				@Override
				public Void fun(StringBuilder t, DotNetTypeRef v)
				{
					appendTypeRef(t, v);
					return null;
				}
			}, ", ");
			builder.append(">");
		}
		else if(typeRef instanceof CSharpRefTypeRef)
		{
			builder.append(((CSharpRefTypeRef) typeRef).getType().name());
			builder.append(" ");
			appendTypeRef(builder, ((CSharpRefTypeRef) typeRef).getInnerTypeRef());
		}
		else if(typeRef instanceof DotNetPointerTypeRef)
		{
			appendTypeRef(builder, ((DotNetPointerTypeRef) typeRef).getInnerTypeRef());
			builder.append("*");
		}
		else
		{
			builder.append(typeRef.getQualifiedText());
		}
	}

	private static <T extends DotNetVirtualImplementOwner & DotNetNamedElement> void appendName(T element, StringBuilder builder)
	{
		DotNetTypeRef typeRefForImplement = element.getTypeRefForImplement();
		if(typeRefForImplement != DotNetTypeRef.ERROR_TYPE)
		{
			appendTypeRef(builder, typeRefForImplement);
			builder.append(".");
			if(element instanceof CSharpArrayMethodDeclaration)
			{
				builder.append("this");
			}
			else
			{
				builder.append(element.getName());
			}
		}
		else
		{
			if(element instanceof CSharpArrayMethodDeclaration)
			{
				builder.append("this");
			}
			else
			{
				builder.append(element.getName());
			}
		}
	}

	private static void processGenericParameterList(StringBuilder builder, DotNetGenericParameterListOwner owner)
	{
		DotNetGenericParameter[] genericParameters = owner.getGenericParameters();
		if(genericParameters.length == 0)
		{
			return;
		}
		builder.append("<");
		StubBlockUtil.join(builder, genericParameters, new PairFunction<StringBuilder, DotNetGenericParameter, Void>()
		{
			@Nullable
			@Override
			public Void fun(StringBuilder t, DotNetGenericParameter v)
			{
				t.append(v.getName());
				return null;
			}
		}, ", ");
		builder.append(">");
	}

	private static void processParameterList(DotNetParameterListOwner declaration, StringBuilder builder, char p1, char p2)
	{
		builder.append(p1);
		StubBlockUtil.join(builder, declaration.getParameters(), new PairFunction<StringBuilder, DotNetParameter, Void>()
		{
			@Nullable
			@Override
			public Void fun(StringBuilder t, DotNetParameter v)
			{
				processModifierList(t, v);
				appendTypeRef(t, v.toTypeRef(false));
				t.append(" ");
				appendValidName(t, v.getName());
				return null;
			}
		}, ", ");
		builder.append(p2);
	}

	private static void processModifierList(StringBuilder builder, DotNetModifierListOwner owner)
	{
		DotNetModifierList modifierList = owner.getModifierList();
		if(modifierList != null)
		{
			for(DotNetModifier dotNetModifier : modifierList.getModifiers())
			{
				if(dotNetModifier == CSharpModifier.REF || dotNetModifier == CSharpModifier.OUT)
				{
					continue;
				}
				builder.append(dotNetModifier.getPresentableText()).append(" ");
			}
		}
	}

	public static void appendValidName(StringBuilder builder, String name)
	{
		if(isKeyword(name))
		{
			builder.append("@");
		}
		builder.append(name);
	}

	public static boolean isKeyword(String str)
	{
		try
		{
			CSharpLexer cSharpLexer = new CSharpLexer();
			cSharpLexer.start(str);
			return CSharpTokenSets.KEYWORDS.contains(cSharpLexer.getTokenType());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public List<StubBlock> getBlocks()
	{
		return myBlocks;
	}
}
