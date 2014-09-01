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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNativeTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNullableTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPointerTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeWithTypeArgumentsImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromText;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetPointerTypeRefImpl;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetUserType;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubInputStream;
import lombok.val;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpStubTypeInfoUtil
{
	private static TIntObjectHashMap<DotNetTypeRef> ourStaticRefs = new TIntObjectHashMap<DotNetTypeRef>();
	private static TObjectIntHashMap<DotNetTypeRef> ourStaticRefs2 = new TObjectIntHashMap<DotNetTypeRef>();

	static
	{
		try
		{
			byte i = 0;
			Field[] fields = CSharpStaticTypeRef.class.getFields();
			for(Field field : fields)
			{
				if(Modifier.isStatic(field.getModifiers()) && field.getType() == CSharpStaticTypeRef.class)
				{
					byte value = i;
					CSharpStaticTypeRef typeRef = (CSharpStaticTypeRef) field.get(null);

					ourStaticRefs.put(value, typeRef);
					ourStaticRefs2.put(typeRef, value);

					i++;
				}
			}
		}
		catch(IllegalAccessException e)
		{
			throw new Error(e);
		}
	}

	public static CSharpStubTypeInfo read(@NotNull StubInputStream stubInputStream) throws IOException
	{
		byte b = stubInputStream.readByte();
		CSharpStubTypeInfo.Id value = CSharpStubTypeInfo.Id.VALUES[b];
		switch(value)
		{
			case USER:
				return new CSharpStubUserTypeInfo(stubInputStream);
			case QUALIFIED:
				return new CSharpStubQualifiedTypeInfo(stubInputStream);
			case POINTER:
				return new CSharpStubPointerTypeInfo(stubInputStream);
			case ARRAY:
				return new CSharpStubArrayTypeInfo(stubInputStream);
			case GENERIC_WRAPPER:
				return new CSharpStubGenericWrapperTypeInfo(stubInputStream);
			case STATIC:
				return new CSharpStubNativeTypeInfo(stubInputStream);
			case NULLABLE:
				return new CSharpStubNullableTypeInfo(stubInputStream);
			case ERROR:
				return CSharpStubErrorInfoType.INSTANCE;
			default:
				throw new IllegalArgumentException();
		}
	}

	@NotNull
	public static CSharpStubTypeInfo toStub(@Nullable DotNetType t)
	{
		if(t == null)
		{
			return CSharpStubErrorInfoType.INSTANCE;
		}
		val ref = new Ref<CSharpStubTypeInfo>();
		t.accept(new CSharpElementVisitor()
		{
			@Override
			public void visitPointerType(CSharpPointerTypeImpl type)
			{
				ref.set(new CSharpStubPointerTypeInfo(toStub(type.getInnerType())));
			}

			@Override
			public void visitNullableType(CSharpNullableTypeImpl type)
			{
				ref.set(new CSharpStubNullableTypeInfo(toStub(type.getInnerType())));
			}

			@Override
			public void visitNativeType(CSharpNativeTypeImpl type)
			{
				DotNetTypeRef typeRef = type.toTypeRef();
				//if(typeRef instanceof CSharpStaticTypeRef)
				{
					int i = ourStaticRefs2.get(typeRef);
					ref.set(new CSharpStubNativeTypeInfo(i));
				}
				/*else
				{
					ref.set(new CSharpStubQualifiedTypeInfo(typeRef.getQualifiedText(), typeRef.isNullable()));
				}  */
			}

			@Override
			public void visitArrayType(CSharpArrayTypeImpl type)
			{
				ref.set(new CSharpStubArrayTypeInfo(toStub(type.getInnerType()), type.getDimensions()));
			}

			@Override
			public void visitTypeWrapperWithTypeArguments(CSharpTypeWithTypeArgumentsImpl typeArguments)
			{
				CSharpStubTypeInfo inner = toStub(typeArguments.getInnerType());

				DotNetType[] arguments = typeArguments.getArguments();
				CSharpStubTypeInfo[] typeInfos = new CSharpStubTypeInfo[arguments.length];
				for(int i = 0; i < arguments.length; i++)
				{
					typeInfos[i] = toStub(arguments[i]);
				}

				ref.set(new CSharpStubGenericWrapperTypeInfo(inner, typeInfos));
			}

			@Override
			public void visitReferenceType(DotNetUserType type)
			{
				ref.set(new CSharpStubUserTypeInfo(type.getReferenceText()));
			}
		});
		return ref.get();
	}

	@NotNull
	public static DotNetTypeRef toTypeRef(CSharpStubTypeInfo typeInfo, PsiElement element)
	{
		switch(typeInfo.getId())
		{
			case ERROR:
				return DotNetTypeRef.ERROR_TYPE;
			case USER:
				CSharpStubUserTypeInfo referenceTypeInfo = (CSharpStubUserTypeInfo) typeInfo;
				return new CSharpTypeRefFromText(referenceTypeInfo.getText(), element);
			case QUALIFIED:
				CSharpStubQualifiedTypeInfo qualifiedTypeInfo = (CSharpStubQualifiedTypeInfo) typeInfo;
				return new DotNetTypeRefByQName(qualifiedTypeInfo.getQualifiedText(), CSharpTransform.INSTANCE, qualifiedTypeInfo.isNullable());
			case POINTER:
				CSharpStubPointerTypeInfo pointerTypeInfo = (CSharpStubPointerTypeInfo) typeInfo;
				return new DotNetPointerTypeRefImpl(toTypeRef(pointerTypeInfo.getInnerType(), element));
			case NULLABLE:
				CSharpStubNullableTypeInfo nullableTypeInfo = (CSharpStubNullableTypeInfo) typeInfo;
				return new DotNetTypeRef.Delegate(toTypeRef(nullableTypeInfo.getInnerType(), element))
				{
					@Override
					public boolean isNullable()
					{
						return true;
					}
				};
			case ARRAY:
				CSharpStubArrayTypeInfo arrayTypeInfo = (CSharpStubArrayTypeInfo) typeInfo;
				return new CSharpArrayTypeRef(toTypeRef(arrayTypeInfo.getInnerType(), element), arrayTypeInfo.getDimensions());
			case GENERIC_WRAPPER:
				CSharpStubGenericWrapperTypeInfo genericWrapperTypeInfo = (CSharpStubGenericWrapperTypeInfo) typeInfo;
				CSharpStubTypeInfo[] arguments = genericWrapperTypeInfo.getArguments();
				DotNetTypeRef[] arguments2 = new DotNetTypeRef[arguments.length];
				for(int i = 0; i < arguments.length; i++)
				{
					CSharpStubTypeInfo argument = arguments[i];
					arguments2[i] = toTypeRef(argument, element);
				}
				return new DotNetGenericWrapperTypeRef(toTypeRef(genericWrapperTypeInfo.getInnerType(), element), arguments2);
			case STATIC:
				CSharpStubNativeTypeInfo nativeTypeInfo = (CSharpStubNativeTypeInfo) typeInfo;
				return ourStaticRefs.get(nativeTypeInfo.getIndex());
			default:
				throw new IllegalArgumentException();
		}
	}
}
