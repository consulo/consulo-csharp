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

package consulo.csharp.lang.impl.psi.resolve.additionalMembersImpl;

import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.ast.IElementType;
import consulo.util.collection.MultiMap;
import consulo.util.jdom.JDOMUtil;
import consulo.util.lang.reflect.ReflectionUtil;
import org.jdom.Document;
import org.jdom.Element;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public class OperatorStubsLoader {
    public static class Operator {
        public static class Parameter {
            public String myTypeRef;

            public Parameter(String type) {
                myTypeRef = type;
            }
        }

        public final IElementType myOperatorToken;
        public final String myReturnTypeRef;
        public final String myBlocker;
        public final List<Parameter> myParameterTypes = new ArrayList<>(5);

        public Operator(String name, String returnType, String blocker) {
            Field declaredField = ReflectionUtil.getDeclaredField(CSharpTokens.class, name);
            assert declaredField != null;
            try {
                myOperatorToken = (IElementType) declaredField.get(null);
            }
            catch (IllegalAccessException e) {
                throw new Error();
            }
            myBlocker = blocker;
            myReturnTypeRef = returnType;
        }
    }

    public MultiMap<String, Operator> myTypeOperators = new MultiMap<>();

    public List<Operator> myEnumOperators = new ArrayList<>();

    private OperatorStubsLoader() {
        try {
            Document document = JDOMUtil.loadDocument(getClass(), "/stub/operatorStubs.xml");
            for (Element e : document.getRootElement().getChildren()) {
                Collection<Operator> list = null;
                if ("type".equals(e.getName())) {
                    String className = e.getAttributeValue("name");
                    list = myTypeOperators.getModifiable(className);
                }
                else if ("enum".equals(e.getName())) {
                    list = myEnumOperators;
                }
                assert list != null;

                for (Element opElement : e.getChildren()) {
                    String operatorName = opElement.getAttributeValue("name");
                    String returnType = opElement.getAttributeValue("type");
                    String blockerVmQName = opElement.getAttributeValue("blocker");

                    Operator operator = new Operator(operatorName, returnType, blockerVmQName);

                    for (Element parameterElement : opElement.getChildren()) {
                        String parameterType = parameterElement.getAttributeValue("type");
                        operator.myParameterTypes.add(new Operator.Parameter(parameterType));
                    }
                    list.add(operator);
                }
            }
        }
        catch (Exception e) {
            throw new Error(e);
        }
    }

    public static OperatorStubsLoader INSTANCE = new OperatorStubsLoader();
}
