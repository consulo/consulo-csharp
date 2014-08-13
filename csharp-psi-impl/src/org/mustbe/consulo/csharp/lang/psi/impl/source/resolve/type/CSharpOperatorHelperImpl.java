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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightParameter;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightGenericConstraintBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightGenericParameterBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightMethodDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAttributeImpl;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 15.03.14
 */
public class CSharpOperatorHelperImpl extends CSharpOperatorHelper
{
	private static String[] ourStubs = new String[]{
			"/stub/ObjectStubs.cs",
			"/stub/EnumStubs.cs",
			"/stub/BoolStubs.cs",
			"/stub/StringStubs.cs",
			"/stub/ByteStubs.cs",
			"/stub/ShortStubs.cs",
			"/stub/IntStubs.cs",
			"/stub/LongStubs.cs",
			"/stub/FloatStubs.cs",
	};

	private Map<IElementType, String> myOperatorNames = new HashMap<IElementType, String>()
	{
		{
			put(CSharpTokens.LT, "<");
			put(CSharpTokens.LTEQ, ">");
			put(CSharpTokens.GT, ">");
			put(CSharpTokens.GTEQ, ">");
			put(CSharpTokens.MUL, "*");
			put(CSharpTokens.MULEQ, "*");
			put(CSharpTokens.DIV, "/");
			put(CSharpTokens.DIVEQ, "/");
			put(CSharpTokens.PLUS, "+");
			put(CSharpTokens.PLUSEQ, "+");
			put(CSharpTokens.PLUSPLUS, "++");
			put(CSharpTokens.MINUS, "-");
			put(CSharpTokens.MINUSEQ, "-");
			put(CSharpTokens.MINUSMINUS, "--");
			put(CSharpTokens.LTLT, "-<<");
			put(CSharpTokens.LTLTEQ, "<<");
			put(CSharpTokens.GTGT, ">>");
			put(CSharpTokens.GTGTEQ, ">>");
			put(CSharpTokens.EXCL, "!");
			put(CSharpTokens.AND, "&");
			put(CSharpTokens.ANDEQ, "&");
			put(CSharpTokens.OR, "|");
			put(CSharpTokens.OREQ, "|");
			put(CSharpTokens.XOR, "^");
			put(CSharpTokens.XOREQ, "^");
			put(CSharpTokens.TILDE, "~");
		}
	};

	private final Project myProject;

	public CSharpOperatorHelperImpl(Project project)
	{
		myProject = project;
	}

	@NotNull
	@Override
	@LazyInstance
	public List<DotNetNamedElement> getStubMembers()
	{
		List<DotNetNamedElement> list = new ArrayList<DotNetNamedElement>();
		for(String stub : ourStubs)
		{
			InputStream resourceAsStream = getClass().getResourceAsStream(stub);
			if(resourceAsStream == null)
			{
				throw new Error("Possible broken build. '" + stub + "' not found");
			}
			try
			{
				String text = FileUtil.loadTextAndClose(resourceAsStream);
				DotNetTypeDeclaration declaration = CSharpFileFactory.createTypeDeclaration(myProject, text);
				for(DotNetNamedElement dotNetNamedElement : declaration.getMembers())
				{
					boolean modify = false;

					CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) dotNetNamedElement;
					DotNetAttribute[] attributes = methodDeclaration.getModifierList().getAttributes();
					for(DotNetAttribute it : attributes)
					{
						CSharpAttributeImpl attribute = (CSharpAttributeImpl) it;

						CSharpReferenceExpression referenceExpression = attribute.getReferenceExpression();
						if(referenceExpression == null)
						{
							continue;
						}
						String referenceExpressionText = referenceExpression.getText();
						if(referenceExpressionText.equals("EnumOperator"))
						{
							modify = true;
							break;
						}
					}

					if(modify)
					{
						CSharpLightMethodDeclarationBuilder builder = new CSharpLightMethodDeclarationBuilder(myProject);
						builder.setOperator(methodDeclaration.getOperatorElementType());


						CSharpLightGenericParameterBuilder genericParameterBuilder = new CSharpLightGenericParameterBuilder(myProject);
						genericParameterBuilder.withName("T");

						CSharpLightGenericConstraintBuilder constraintBuilder = new CSharpLightGenericConstraintBuilder(genericParameterBuilder);
						constraintBuilder.addTypeConstraint(new CSharpTypeRefFromText(DotNetTypes.System_Enum, builder));

						builder.addGenericParameter(genericParameterBuilder);
						builder.addGenericConstraint(constraintBuilder);

						builder.addModifier(CSharpModifier.PUBLIC).addModifier(CSharpModifier.STATIC);

						if(isTextEqual(methodDeclaration.getReturnType(), "T"))
						{
							builder.withReturnType(new CSharpTypeRefFromGenericParameter(genericParameterBuilder));
						}
						else
						{
							builder.withReturnType(methodDeclaration.getReturnTypeRef());
						}

						for(DotNetParameter parameter : methodDeclaration.getParameters())
						{
							if(isTextEqual(parameter.getType(), "T"))
							{
								CSharpLightParameterBuilder b = new CSharpLightParameterBuilder(parameter);
								b.withName(parameter.getName());
								b.withTypeRef(new CSharpTypeRefFromGenericParameter(genericParameterBuilder));

								builder.addParameter(b);
							}
							else
							{
								builder.addParameter(new CSharpLightParameter(parameter));
							}
						}

						list.add(builder);
					}
					else
					{
						list.add(dotNetNamedElement);
					}
				}
			}
			catch(IOException e)
			{
				throw new Error("Possible broken build. '" + stub + "' not found");
			}
		}
		return list;
	}

	@Nullable
	@Override
	public String getOperatorName(@NotNull IElementType elementType)
	{
		return myOperatorNames.get(elementType);
	}

	private static boolean isTextEqual(PsiElement element, String text)
	{
		return element.getText().equals(text);
	}
}
