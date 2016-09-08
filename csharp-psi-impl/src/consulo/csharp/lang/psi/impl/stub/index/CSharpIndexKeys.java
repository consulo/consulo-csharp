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

package consulo.csharp.lang.psi.impl.stub.index;

import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.dotnet.psi.DotNetEventDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import com.intellij.psi.stubs.StubIndexKey;

/**
 * @author VISTALL
 * @since 16.12.13.
 */
public interface CSharpIndexKeys
{
	StubIndexKey<String, DotNetQualifiedElement> MEMBER_BY_NAMESPACE_QNAME_INDEX =
			StubIndexKey.createIndexKey("csharp.member.by.namespace.qname.index");
	StubIndexKey<String, DotNetQualifiedElement> MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX =
			StubIndexKey.createIndexKey("csharp.member.by.all.namespace.qname.index");

	StubIndexKey<String, DotNetLikeMethodDeclaration> METHOD_INDEX = StubIndexKey.createIndexKey("csharp.method.index");
	StubIndexKey<String, DotNetLikeMethodDeclaration> EXTENSION_METHOD_BY_NAME_INDEX =
			StubIndexKey.createIndexKey("csharp.extension.method.by.name.index");
	StubIndexKey<String, CSharpMethodDeclaration> DELEGATE_METHOD_BY_NAME_INDEX =
			StubIndexKey.createIndexKey("csharp.delegate.method.by.name.index");

	StubIndexKey<String, DotNetTypeDeclaration> TYPE_WITH_EXTENSION_METHODS_INDEX =
			StubIndexKey.createIndexKey("csharp.type.with.extensions.index");

	StubIndexKey<String, CSharpTypeDeclaration> TYPE_INDEX = StubIndexKey.createIndexKey("csharp.type.index");
	StubIndexKey<String, DotNetTypeDeclaration> TYPE_BY_QNAME_INDEX = StubIndexKey.createIndexKey("csharp.type.by.qname.index");
	StubIndexKey<String, DotNetTypeDeclaration> TYPE_BY_VMQNAME_INDEX = StubIndexKey.createIndexKey("csharp.type.by.vm.qname.index");

	StubIndexKey<String, DotNetEventDeclaration> EVENT_INDEX = StubIndexKey.createIndexKey("csharp.event.index");
	StubIndexKey<String, DotNetPropertyDeclaration> PROPERTY_INDEX = StubIndexKey.createIndexKey("csharp.property.index");
	StubIndexKey<String, DotNetFieldDeclaration> FIELD_INDEX = StubIndexKey.createIndexKey("csharp.field.index");

	StubIndexKey<String, DotNetTypeList> EXTENDS_LIST_INDEX = StubIndexKey.createIndexKey("csharp.extends.list.index");

	StubIndexKey<DotNetAttributeTargetType, CSharpAttributeList> ATTRIBUTE_LIST_INDEX = StubIndexKey.createIndexKey("csharp.attribute.list.index");
}
