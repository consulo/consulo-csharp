/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.resolve.additionalMembersImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.containers.MultiMap;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public class OperatorStubsLoader
{
	public static class Operator
	{
		public static class Parameter
		{
			public DotNetTypeRef myTypeRef;

			public Parameter(String type)
			{
				myTypeRef = type == null ? null : new CSharpTypeRefByQName(type);
			}
		}

		public final IElementType myOperatorToken;
		public final DotNetTypeRef myReturnTypeRef;
		public final List<Parameter> myParameterTypes = new ArrayList<Parameter>(5);

		public Operator(String name, String returnType)
		{
			Field declaredField = ReflectionUtil.getDeclaredField(CSharpTokens.class, name);
			assert declaredField != null;
			try
			{
				myOperatorToken = (IElementType) declaredField.get(null);
			}
			catch(IllegalAccessException e)
			{
				throw new Error();
			}
			myReturnTypeRef = returnType == null ? null : new CSharpTypeRefByQName(returnType);
		}
	}

	public MultiMap<String, Operator> myTypeOperators = new MultiMap<String, Operator>();

	public List<Operator> myEnumOperators = new ArrayList<Operator>();

	private OperatorStubsLoader()
	{
		try
		{
			Document document = JDOMUtil.loadDocument(getClass(), "/stub/operatorStubs.xml");
			for(Element e : document.getRootElement().getChildren())
			{
				Collection<Operator> list = null;
				if("type".equals(e.getName()))
				{
					String className = e.getAttributeValue("name");
					list = myTypeOperators.getModifiable(className);
				}
				else if("enum".equals(e.getName()))
				{
					list = myEnumOperators;
				}
				assert list != null;

				for(Element opElement : e.getChildren())
				{
					String operatorName = opElement.getAttributeValue("name");
					String returnType = opElement.getAttributeValue("type");

					Operator operator = new Operator(operatorName, returnType);

					for(Element parameterElement : opElement.getChildren())
					{
						String parameterType = parameterElement.getAttributeValue("type");
						operator.myParameterTypes.add(new Operator.Parameter(parameterType));
					}
					list.add(operator);
				}
			}
		}
		catch(Exception e)
		{
			throw new Error(e);
		}
	}

	public static OperatorStubsLoader INSTANCE = new OperatorStubsLoader();
}
