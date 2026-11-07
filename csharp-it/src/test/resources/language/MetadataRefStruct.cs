// sdk:NET_10
// a ref struct of a library is a struct with IsByRefLikeAttribute
using Lib;

class Test
{
    void Method()
    {
        /*modifier:ref*/Buffer buffer = default;
        var /*type:int*/length = buffer.Length;
        Plain plain = default;
    }
}
