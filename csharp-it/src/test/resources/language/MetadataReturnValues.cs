// sdk:NET_10
// the return value of a library method - by reference, and with the attributes of the return value
using System;
using Lib;

class Test
{
    void Method(Returns returns, int[] array)
    {
        var /*type:(int Id, string Name)*/tuple = Returns.Tuple();
        var /*type:string*/name = Returns.Tuple().Name;
        var /*type:dynamic*/value = Returns.Dynamic();
        var /*type:int*/byRef = returns.Ref();
        ref int reference = ref returns.Ref();
        var /*type:int*/readOnly = returns.RefReadonly();
        var /*type:int*/property = returns.Property;

        ReadOnlySpan<int> span = array;
        var /*type:int*/item = span[0];
        foreach (var /*type:int*/element in span)
        {
        }
        var /*type:int*/count = Returns.Span(1, 2, 3);
    }
}
