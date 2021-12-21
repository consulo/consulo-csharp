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

package consulo.csharp.ide.msil.representation.builder;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.PairFunction;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import consulo.internal.dotnet.msil.decompiler.textBuilder.block.LineStubBlock;
import consulo.internal.dotnet.msil.decompiler.textBuilder.block.StubBlock;
import consulo.internal.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;
import consulo.msil.lang.psi.MsilConstantValue;
import consulo.msil.lang.psi.MsilCustomAttribute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author VISTALL
 * @since 02.06.14
 */
public class CSharpStubBuilderVisitor extends CSharpElementVisitor
{
	@Nonnull
	@RequiredReadAction
	public static List<StubBlock> buildBlocks(PsiElement qualifiedElement)
	{
		return buildBlocks(qualifiedElement, true);
	}

	@Nonnull
	@RequiredReadAction
	public static List<StubBlock> buildBlocks(PsiElement qualifiedElement, boolean compiled)
	{
		CSharpStubBuilderVisitor visitor = new CSharpStubBuilderVisitor(compiled);
		qualifiedElement.accept(visitor);
		return visitor.getBlocks();
	}

	private boolean myCompiled;

	private List<StubBlock> myBlocks = new ArrayList<StubBlock>(2);

	public CSharpStubBuilderVisitor(boolean compiled)
	{
		myCompiled = compiled;
	}

	@Override
	@RequiredReadAction
	public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processAttributeListAsLine(declaration, myBlocks);
		processModifierList(builder, declaration);
		appendTypeRef(declaration.getProject(), builder, declaration.toTypeRef(false));
		builder.append(" ");
		appendName(declaration, builder);
		StubBlock e = new StubBlock(builder, null, StubBlock.BRACES);
		for(DotNetXAccessor DotNetXAccessor : declaration.getAccessors())
		{
			e.getBlocks().addAll(buildBlocks(DotNetXAccessor, myCompiled));
		}
		myBlocks.add(e);
	}

	@Override
	@RequiredReadAction
	public void visitXAccessor(DotNetXAccessor accessor)
	{
		DotNetXAccessor.Kind accessorKind = accessor.getAccessorKind();
		if(accessorKind == null)
		{
			return;
		}

		StringBuilder builder = new StringBuilder();

		processModifierList(builder, accessor);

		builder.append(accessorKind.name().toLowerCase(Locale.US));

		boolean canHaveBody = !accessor.hasModifier(CSharpModifier.ABSTRACT);

		if(canHaveBody)
		{
			builder.append("; //compiled code\n");
		}
		else
		{
			builder.append(";\n");
		}
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	@RequiredReadAction
	public void visitEnumConstantDeclaration(CSharpEnumConstantDeclaration declaration)
	{
		processAttributeListAsLine(declaration, myBlocks);
		StringBuilder builder = new StringBuilder();
		builder.append(declaration.getName());
		appendInitializer(builder, declaration);
		builder.append(",\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	@RequiredReadAction
	public void visitFieldDeclaration(CSharpFieldDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processAttributeListAsLine(declaration, myBlocks);
		processModifierList(builder, declaration);
		if(declaration.isConstant())
		{
			builder.append("const ");
		}
		appendTypeRef(declaration.getProject(), builder, declaration.toTypeRef(false));
		builder.append(" ");
		appendValidName(builder, declaration.getName());
		appendInitializer(builder, declaration);
		builder.append(";\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	@RequiredReadAction
	public void visitEventDeclaration(CSharpEventDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processAttributeListAsLine(declaration, myBlocks);
		processModifierList(builder, declaration);
		builder.append("event ");
		appendTypeRef(declaration.getProject(), builder, declaration.toTypeRef(false));
		builder.append(" ");
		appendName(declaration, builder);

		StubBlock e = new StubBlock(builder, null, StubBlock.BRACES);
		for(DotNetXAccessor DotNetXAccessor : declaration.getAccessors())
		{
			e.getBlocks().addAll(buildBlocks(DotNetXAccessor, myCompiled));
		}
		myBlocks.add(e);
	}

	@Override
	@RequiredReadAction
	public void visitIndexMethodDeclaration(CSharpIndexMethodDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		processAttributeListAsLine(declaration, myBlocks);
		processModifierList(builder, declaration);
		appendTypeRef(declaration.getProject(), builder, declaration.getReturnTypeRef());
		builder.append(" ");
		appendName(declaration, builder);
		processParameterList(declaration, builder, '[', ']');

		StubBlock e = new StubBlock(builder, null, StubBlock.BRACES);
		for(DotNetXAccessor DotNetXAccessor : declaration.getAccessors())
		{
			e.getBlocks().addAll(buildBlocks(DotNetXAccessor, myCompiled));
		}
		myBlocks.add(e);
	}

	@Override
	@RequiredReadAction
	public void visitConversionMethodDeclaration(CSharpConversionMethodDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		processAttributeListAsLine(declaration, myBlocks);
		processModifierList(builder, declaration);
		appendTypeRef(declaration.getProject(), builder, declaration.getConversionTypeRef());
		builder.append(" ");
		builder.append("operator ");
		appendTypeRef(declaration.getProject(), builder, declaration.getReturnTypeRef());
		processParameterList(declaration, builder, '(', ')');
		builder.append("; //compiled code\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	@RequiredReadAction
	public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		if(declaration.isDeConstructor())
		{
			builder.append("~");
		}
		else
		{
			processAttributeListAsLine(declaration, myBlocks);
			processModifierList(builder, declaration);
		}

		builder.append(declaration.getName());
		processParameterList(declaration, builder, '(', ')');
		builder.append("; //compiled code\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	@RequiredReadAction
	public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		processAttributeListAsLine(declaration, myBlocks);
		processModifierList(builder, declaration);

		if(declaration.isDelegate())
		{
			builder.append("delegate ");
		}
		appendTypeRef(declaration.getProject(), builder, declaration.getReturnTypeRef());
		builder.append(" ");
		if(declaration.isOperator())
		{
			builder.append("operator ");
		}
		appendName(declaration, builder);
		processGenericParameterList(builder, declaration);
		processParameterList(declaration, builder, '(', ')');
		processGenericConstraintList(builder, declaration);

		if(myCompiled)
		{
			boolean canHaveBody = !declaration.hasModifier(CSharpModifier.ABSTRACT) && !declaration.isDelegate();

			if(canHaveBody)
			{
				builder.append("; //compiled code\n");
			}
			else
			{
				builder.append(";\n");
			}
		}
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	@RequiredReadAction
	public void visitTypeDeclaration(final CSharpTypeDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processAttributeListAsLine(declaration, myBlocks);
		processModifierList(builder, declaration);

		if(declaration.isEnum())
		{
			builder.append("enum ");
		}
		else if(declaration.isStruct())
		{
			builder.append("struct ");
		}
		else if(declaration.isInterface())
		{
			builder.append("interface ");
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
			if(!DotNetTypeRefUtil.isVmQNameEqual(typeRefForEnumConstants, DotNetTypes.System.Int32))
			{
				builder.append(" : ");
				appendTypeRef(declaration.getProject(), builder, typeRefForEnumConstants);
			}
		}
		else
		{
			DotNetTypeRef[] extendTypeRefs = declaration.getExtendTypeRefs();
			List<DotNetTypeRef> temp = ContainerUtil.filter(extendTypeRefs, new Condition<DotNetTypeRef>()
			{
				@Override
				@RequiredReadAction
				public boolean value(DotNetTypeRef typeRef)
				{
					return !DotNetTypeRefUtil.isVmQNameEqual(typeRef, DotNetTypes.System.Object) && !DotNetTypeRefUtil.isVmQNameEqual(typeRef, DotNetTypes.System.ValueType);
				}
			});

			if(!temp.isEmpty())
			{
				builder.append(" : ");

				StubBlockUtil.join(builder, temp, new PairFunction<StringBuilder, DotNetTypeRef, Void>()
				{
					@Nullable
					@Override
					@RequiredReadAction
					public Void fun(StringBuilder builder, DotNetTypeRef typeRef)
					{
						appendTypeRef(declaration.getProject(), builder, typeRef);
						return null;
					}
				}, ", ");
			}
		}
		processGenericConstraintList(builder, declaration);
		StubBlock e = new StubBlock(builder, null, StubBlock.BRACES);
		myBlocks.add(e);

		for(DotNetNamedElement dotNetNamedElement : declaration.getMembers())
		{
			e.getBlocks().addAll(buildBlocks(dotNetNamedElement, myCompiled));
		}
	}

	@RequiredReadAction
	public static void appendTypeRef(@Nonnull Project project, @Nonnull StringBuilder builder, @Nonnull DotNetTypeRef typeRef)
	{
		CSharpTypeRefPresentationUtil.appendTypeRef(builder, typeRef, CSharpTypeRefPresentationUtil.QUALIFIED_NAME | CSharpTypeRefPresentationUtil.FORCE_TYPE_KEYWORD);
	}

	@RequiredReadAction
	private static <T extends DotNetVirtualImplementOwner & DotNetNamedElement> void appendName(T element, StringBuilder builder)
	{
		DotNetTypeRef typeRefForImplement = element.getTypeRefForImplement();
		if(typeRefForImplement != DotNetTypeRef.ERROR_TYPE)
		{
			appendTypeRef(element.getProject(), builder, typeRefForImplement);
			builder.append(".");
			if(element instanceof CSharpIndexMethodDeclaration)
			{
				builder.append("this");
			}
			else
			{
				appendValidName(builder, element.getName());
			}
		}
		else
		{
			if(element instanceof CSharpIndexMethodDeclaration)
			{
				builder.append("this");
			}
			else
			{
				appendValidName(builder, element.getName());
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
			@RequiredReadAction
			public Void fun(StringBuilder t, DotNetGenericParameter v)
			{
				appendAttributeList(v, t, v.getAttributes());

				if(v.hasModifier(CSharpModifier.OUT))
				{
					t.append("out ");
				}
				else if(v.hasModifier(CSharpModifier.IN))
				{
					t.append("in ");
				}
				t.append(v.getName());
				return null;
			}
		}, ", ");
		builder.append(">");
	}

	private static void processGenericConstraintList(final StringBuilder builder, final CSharpGenericConstraintOwner owner)
	{
		CSharpGenericConstraint[] genericConstraints = owner.getGenericConstraints();
		if(genericConstraints.length == 0)
		{
			return;
		}

		builder.append(" ");
		StubBlockUtil.join(builder, genericConstraints, new PairFunction<StringBuilder, CSharpGenericConstraint, Void>()
		{
			@Nullable
			@Override
			public Void fun(final StringBuilder builder, CSharpGenericConstraint v)
			{
				builder.append("where ");
				DotNetGenericParameter resolve = v.resolve();
				assert resolve != null;
				builder.append(resolve.getName());
				builder.append(" : ");

				CSharpGenericConstraintValue[] genericConstraintValues = v.getGenericConstraintValues();
				for(CSharpGenericConstraintValue genericConstraintValue : genericConstraintValues)
				{
					genericConstraintValue.accept(new CSharpElementVisitor()
					{
						@Override
						public void visitGenericConstraintKeywordValue(CSharpGenericConstraintKeywordValue value)
						{
							IElementType keywordElementType = value.getKeywordElementType();
							if(keywordElementType == CSharpTokens.STRUCT_KEYWORD)
							{
								builder.append("struct");
							}
							else if(keywordElementType == CSharpTokens.CLASS_KEYWORD)
							{
								builder.append("class");
							}
							else if(keywordElementType == CSharpTokens.NEW_KEYWORD)
							{
								builder.append("new()");
							}
						}

						@Override
						@RequiredReadAction
						public void visitGenericConstraintTypeValue(CSharpGenericConstraintTypeValue value)
						{
							appendTypeRef(owner.getProject(), builder, value.toTypeRef());
						}
					});
				}
				return null;
			}
		}, " ");
	}

	private static void processParameterList(final DotNetParameterListOwner declaration, StringBuilder builder, char p1, char p2)
	{
		builder.append(p1);
		StubBlockUtil.join(builder, declaration.getParameters(), new PairFunction<StringBuilder, DotNetParameter, Void>()
		{
			@Nullable
			@Override
			@RequiredReadAction
			public Void fun(StringBuilder t, DotNetParameter v)
			{
				appendAttributeList(t, v);
				processModifierList(t, v);
				DotNetTypeRef typeRef = v.toTypeRef(false);
				appendTypeRef(declaration.getProject(), t, typeRef);
				if(typeRef != CSharpStaticTypeRef.__ARGLIST_TYPE)
				{
					t.append(" ");
					appendValidName(t, v.getName());
					appendInitializer(t, v);
				}
				return null;
			}
		}, ", ");
		builder.append(p2);
	}

	@RequiredReadAction
	public static void processAttributeListAsLine(DotNetModifierListOwner owner, List<StubBlock> blocks)
	{
		DotNetModifierList modifierList = owner.getModifierList();
		if(modifierList == null)
		{
			return;
		}
		processAttributeListAsLine(owner, blocks, null, modifierList.getAttributes());
	}

	@RequiredReadAction
	public static void processAttributeListAsLine(PsiElement scope, List<StubBlock> blocks, DotNetAttributeTargetType targetType, DotNetAttribute[] attributes)
	{
		for(DotNetAttribute dotNetAttribute : attributes)
		{
			StringBuilder builder = new StringBuilder();
			builder.append("[");

			if(targetType != null)
			{
				builder.append(targetType.name().toLowerCase(Locale.US)).append(": ");
			}
			appendTypeRef(scope.getProject(), builder, dotNetAttribute.toTypeRef());

			if(dotNetAttribute instanceof CSharpAttribute)
			{
				DotNetExpression[] parameterExpressions = ((CSharpAttribute) dotNetAttribute).getParameterExpressions();
				if(parameterExpressions.length > 0)
				{
					builder.append("(");
					StubBlockUtil.join(builder, parameterExpressions, new PairFunction<StringBuilder, DotNetExpression, Void>()
					{
						@Nullable
						@Override
						@RequiredReadAction
						public Void fun(StringBuilder builder, DotNetExpression dotNetExpression)
						{
							builder.append(dotNetExpression.getText());
							return null;
						}
					}, ", ");

					builder.append(")");
				}
			}
			else if(dotNetAttribute instanceof MsilCustomAttribute)
			{
				CSharpAttributeStubBuilder.append(builder, (MsilCustomAttribute) dotNetAttribute);
			}
			builder.append("]");
			blocks.add(new LineStubBlock(builder));
		}
	}

	@RequiredReadAction
	private static void appendAttributeList(final StringBuilder builder, final DotNetModifierListOwner owner)
	{
		DotNetModifierList modifierList = owner.getModifierList();
		if(modifierList == null)
		{
			return;
		}

		DotNetAttribute[] attributes = modifierList.getAttributes();
		if(attributes.length == 0)
		{
			return;
		}
		appendAttributeList(owner, builder, attributes);
	}

	@RequiredReadAction
	private static void appendAttributeList(final PsiElement scope, final StringBuilder builder, final DotNetAttribute[] attributes)
	{
		if(attributes.length == 0)
		{
			return;
		}

		builder.append("[");
		StubBlockUtil.join(builder, attributes, new PairFunction<StringBuilder, DotNetAttribute, Void>()
		{
			@Override
			@RequiredReadAction
			public Void fun(StringBuilder builder, DotNetAttribute dotNetAttribute)
			{
				appendTypeRef(scope.getProject(), builder, dotNetAttribute.toTypeRef());

				if(dotNetAttribute instanceof CSharpAttribute)
				{
					DotNetExpression[] parameterExpressions = ((CSharpAttribute) dotNetAttribute).getParameterExpressions();
					if(parameterExpressions.length > 0)
					{
						builder.append("(");

						StubBlockUtil.join(builder, parameterExpressions, new PairFunction<StringBuilder, DotNetExpression, Void>()
						{
							@Nullable
							@Override
							@RequiredReadAction
							public Void fun(StringBuilder builder, DotNetExpression dotNetExpression)
							{
								builder.append(dotNetExpression.getText());
								return null;
							}
						}, ", ");

						builder.append(")");
					}
				}
				else if(dotNetAttribute instanceof MsilCustomAttribute)
				{
					CSharpAttributeStubBuilder.append(builder, (MsilCustomAttribute) dotNetAttribute);
				}
				return null;
			}
		}, ", ");
		builder.append("] ");
	}

	@RequiredReadAction
	private static void processModifierList(StringBuilder builder, DotNetModifierListOwner owner)
	{
		DotNetModifierList modifierList = owner.getModifierList();
		if(modifierList == null)
		{
			return;
		}

		for(DotNetModifier dotNetModifier : modifierList.getModifiers())
		{
			if(dotNetModifier == CSharpModifier.OUT || dotNetModifier == CSharpModifier.IN)
			{
				continue;
			}

			if(owner instanceof DotNetVariable && ((DotNetVariable) owner).isConstant() && dotNetModifier == CSharpModifier.STATIC)
			{
				continue;
			}

			if(dotNetModifier == CSharpModifier.ABSTRACT && (isInterface(owner) || isInterface(owner.getParent())))
			{
				continue;
			}

			if(dotNetModifier == CSharpModifier.PUBLIC && isInterface(owner.getParent()))
			{
				continue;
			}

			if(dotNetModifier == CSharpModifier.VIRTUAL && CSharpMethodUtil.isDelegate(owner))
			{
				continue;
			}

			if(dotNetModifier == CSharpModifier.STATIC && owner instanceof DotNetXAccessor)
			{
				continue;
			}

			if((dotNetModifier == CSharpModifier.ABSTRACT || dotNetModifier == CSharpModifier.PUBLIC) && owner instanceof DotNetXAccessor &&
					isInterface(owner.getParent().getParent()))
			{
				continue;
			}

			builder.append(dotNetModifier.getPresentableText()).append(" ");
		}
	}

	private static boolean isInterface(@Nullable PsiElement element)
	{
		return element instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) element).isInterface();
	}

	public static void appendValidName(StringBuilder builder, String name)
	{
		if(CSharpNameSuggesterUtil.isKeyword(name))
		{
			builder.append("@");
		}
		builder.append(name);
	}

	@RequiredReadAction
	private static void appendInitializer(StringBuilder builder, DotNetVariable variable)
	{
		DotNetExpression initializer = variable.getInitializer();
		if(initializer instanceof MsilConstantValue)
		{
			IElementType valueType = ((MsilConstantValue) initializer).getValueType();
			if(valueType == null)
			{
				return;
			}
			String valueText = ((MsilConstantValue) initializer).getValueText();
			if(valueText == null)
			{
				return;
			}
			builder.append(" = ").append(valueText);
		}
		else if(initializer != null)
		{
			builder.append(" = ").append(initializer.getText());
		}
	}

	public List<StubBlock> getBlocks()
	{
		return myBlocks;
	}
}
