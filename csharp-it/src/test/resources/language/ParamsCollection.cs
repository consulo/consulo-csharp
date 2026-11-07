// sdk:NET_10
// params of a collection (C# 13) - the arguments are its elements
using System.Collections.Generic;

class Test
{
    static int List(params List<string> values) => values.Count;

    static int Enumerable(params IEnumerable<long> values) => 0;

    static int Array(params int[] values) => values.Length;

    void Method(List<string> strings)
    {
        var /*type:int*/list = List("a", "b");
        var /*type:int*/one = List("a");
        var /*type:int*/none = List();
        var /*type:int*/whole = List(strings);
        var /*type:int*/widened = Enumerable(1, 2);
        var /*type:int*/array = Array(1, 2);
    }
}
