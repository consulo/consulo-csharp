// sdk:NET_10
// an in parameter of a library is a byref one with IsReadOnlyAttribute: it takes values too; a readonly struct has it too
using Lib;

class Test
{
    void Method(int value)
    {
        var /*type:int*/literal = ReadOnlyRefs.In(1);
        var /*type:int*/variable = ReadOnlyRefs.In(value);
        var /*type:int*/inArgument = ReadOnlyRefs.In(in value);
        var /*type:int*/refReadonly = ReadOnlyRefs.RefReadonly(in value);
        var /*type:int*/reference = ReadOnlyRefs.Ref(ref value);
        /*modifier:readonly*//*attribute:!System.Runtime.CompilerServices.IsReadOnlyAttribute*/Point point = new Point(1);
        var /*type:int*/x = point.X;
    }
}
