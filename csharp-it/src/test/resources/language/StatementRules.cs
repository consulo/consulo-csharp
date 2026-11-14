using System;
using System.Collections.Generic;
class Box { }
struct S { public string s; }
struct U { public int a; public double b; }
unsafe class C
{
    void Items() { }
    void M(bool b, int i, object o, List<int> list, int[] array, string text)
    {
        if (b) /*error:CS1023*/int y = 1;
        while (b) /*error:CS1023*/var z = 2;
        if (b) { int w = 1; Console.WriteLine(w); }
        if (b)/*warning:CS0642*/;
        while (b);
        lock (/*error:CS0185*/i) { }
        lock (o) { }
        foreach (var x in /*error:CS1579*/i) { }
        foreach (var x in /*error:CS0446*//*unresolved*/Items) { }
        foreach (var x in list) { }
        foreach (var x in array) { }
        foreach (var x in text) { }
        /*warning:CS8500*/Box* p = null;
        /*warning:CS8500*/S* q = null;
        U* u = null;
        int* r = null;
        void* v = null;
        Console.WriteLine((long) p + (long) q + (long) u + (long) r + (long) v);
    }
}
