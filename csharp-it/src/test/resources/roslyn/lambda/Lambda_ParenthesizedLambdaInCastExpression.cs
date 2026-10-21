using System;
using System.Collections.Generic;
class Program
{
    static void Main()
    {
        int x = 1;
        byte y = (byte) (/*@type:System.Int32*/x + x);
        Func<int> f1 = (() => { return 1; });
        Func<int> f2 = (Func<int>) (() => { return 2; });
    }
}
