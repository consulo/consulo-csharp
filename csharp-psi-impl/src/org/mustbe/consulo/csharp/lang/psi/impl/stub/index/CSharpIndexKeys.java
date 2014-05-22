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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.index;

import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingListImpl;
import org.mustbe.consulo.dotnet.psi.DotNetEventDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetPropertyDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubIndexKey;

/**
 * @author VISTALL
 * @since 16.12.13.
 */
public interface CSharpIndexKeys
{
	StubIndexKey<String, PsiElement> MEMBER_BY_NAMESPACE_QNAME_INDEX = StubIndexKey.createIndexKey("csharp.member.by.namespace.qname.index");

	StubIndexKey<String, PsiElement> NAMESPACE_BY_QNAME_INDEX = StubIndexKey.createIndexKey("csharp.namespace.by.qname.index");
	StubIndexKey<String, DotNetLikeMethodDeclaration> METHOD_INDEX = StubIndexKey.createIndexKey("csharp.method.index");
	StubIndexKey<String, DotNetLikeMethodDeclaration> EXTENSION_METHOD_INDEX = StubIndexKey.createIndexKey("csharp.extension.method.index");
	StubIndexKey<String, DotNetTypeDeclaration> TYPE_INDEX = StubIndexKey.createIndexKey("csharp.type.index");
	StubIndexKey<String, DotNetTypeDeclaration> TYPE_BY_QNAME_INDEX = StubIndexKey.createIndexKey("csharp.type.by.qname.index");

	StubIndexKey<String, DotNetEventDeclaration> EVENT_INDEX = StubIndexKey.createIndexKey("csharp.event.index");
	StubIndexKey<String, DotNetPropertyDeclaration> PROPERTY_INDEX = StubIndexKey.createIndexKey("csharp.property.index");
	StubIndexKey<String, DotNetFieldDeclaration> FIELD_INDEX = StubIndexKey.createIndexKey("csharp.field.index");

	StubIndexKey<String, DotNetTypeList> EXTENDS_LIST_INDEX = StubIndexKey.createIndexKey("csharp.extends.list.index");
	StubIndexKey<String, CSharpUsingListImpl> USING_LIST_INDEX = StubIndexKey.createIndexKey("csharp.using.list.index");
}
