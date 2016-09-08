package org.mustbe.consulo.csharp.lang.psi.impl.source;

import gnu.trove.THashMap;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpNativeType;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpDynamicTypeRef;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpNativeTypeImplUtil
{
	public static final Map<IElementType, String> ourElementToQTypes = new THashMap<IElementType, String>()
	{
		{
			put(CSharpTokens.BOOL_KEYWORD, DotNetTypes.System.Boolean);
			put(CSharpTokens.DOUBLE_KEYWORD, DotNetTypes.System.Double);
			put(CSharpTokens.FLOAT_KEYWORD, DotNetTypes.System.Single);
			put(CSharpTokens.CHAR_KEYWORD, DotNetTypes.System.Char);
			put(CSharpTokens.OBJECT_KEYWORD, DotNetTypes.System.Object);
			put(CSharpTokens.STRING_KEYWORD, DotNetTypes.System.String);
			put(CSharpTokens.SBYTE_KEYWORD, DotNetTypes.System.SByte);
			put(CSharpTokens.BYTE_KEYWORD, DotNetTypes.System.Byte);
			put(CSharpTokens.INT_KEYWORD, DotNetTypes.System.Int32);
			put(CSharpTokens.UINT_KEYWORD, DotNetTypes.System.UInt32);
			put(CSharpTokens.LONG_KEYWORD, DotNetTypes.System.Int64);
			put(CSharpTokens.ULONG_KEYWORD, DotNetTypes.System.UInt64);
			put(CSharpTokens.VOID_KEYWORD, DotNetTypes.System.Void);
			put(CSharpTokens.SHORT_KEYWORD, DotNetTypes.System.Int16);
			put(CSharpTokens.USHORT_KEYWORD, DotNetTypes.System.UInt16);
			put(CSharpTokens.DECIMAL_KEYWORD, DotNetTypes.System.Decimal);
		}
	};

	@NotNull
	@RequiredReadAction
	public static DotNetTypeRef toTypeRef(@NotNull CSharpNativeType nativeType)
	{
		IElementType elementType = nativeType.getTypeElementType();
		if(elementType == CSharpSoftTokens.VAR_KEYWORD)
		{
			return DotNetTypeRef.AUTO_TYPE;
		}
		else if(elementType == CSharpTokens.IMPLICIT_KEYWORD)
		{
			return CSharpStaticTypeRef.IMPLICIT;
		}
		else if(elementType == CSharpTokens.EXPLICIT_KEYWORD)
		{
			return CSharpStaticTypeRef.EXPLICIT;
		}
		else if(elementType == CSharpTokens.DYNAMIC_KEYWORD)
		{
			return new CSharpDynamicTypeRef(nativeType.getProject(), nativeType.getResolveScope());
		}
		else if(elementType == CSharpTokens.__ARGLIST_KEYWORD)
		{
			return CSharpStaticTypeRef.__ARGLIST_TYPE;
		}

		String qualifiedName = ourElementToQTypes.get(elementType);
		assert qualifiedName != null : elementType.toString();
		return new CSharpTypeRefByQName(nativeType, qualifiedName);
	}
}
