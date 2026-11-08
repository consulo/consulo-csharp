// sdk:NET_10
// nint and nuint (C# 9) are System.IntPtr and System.UIntPtr - no keywords, a declaration named so wins
class Test
{
    void Method(int value, long wide, uint unsigned, byte small)
    {
        nint /*type:System.IntPtr*/fromInt = value;
        nuint /*type:System.UIntPtr*/fromLiteral = 1;
        var /*type:System.IntPtr*/sum = fromInt + 1;
        var /*type:System.IntPtr*/product = fromInt * fromInt;
        long /*type:long*/widened = fromInt;
        var /*type:System.IntPtr*/max = nint.MaxValue;
        var /*type:int*/size = sizeof(nint);
        var /*type:System.IntPtr[]*/array = new nint[] { 1, 2 };
        System.IntPtr /*type:System.IntPtr*/same = fromInt;
        nint /*type:System.IntPtr*/narrowed = (nint)wide;
        nuint /*type:System.UIntPtr*/unsignedNative = fromLiteral;

        // the predefined operators of nint and nuint
        var /*type:long*/withUnsigned = fromInt + unsigned;
        var /*type:long*/withLong = fromInt + wide;
        var /*type:System.UIntPtr*/unsignedSum = unsignedNative + unsigned;
        var /*type:System.UIntPtr*/unsignedByte = unsignedNative + small;
        var /*type:System.IntPtr*/negated = -fromInt;
        var /*type:System.UIntPtr*/complement = ~unsignedNative;
        var /*type:System.IntPtr*/shifted = fromInt << 1;
    }
}

namespace Declared
{
    class nint
    {
    }

    class Use
    {
        void Method()
        {
            nint /*type:Declared.nint*/declared = new nint();
        }
    }
}
