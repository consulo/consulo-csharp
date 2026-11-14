using System;
using System.IO;
class C
{
    readonly int r = 0;
    static readonly int sr = 0;
    int W { set { } }
    static void Ref(ref int x) { }
    static void Out(out int x) { x = 0; }
    static void In(in int x) { }
    void M(C other)
    {
        try { }
        catch (Exception) { }
        catch (/*error:CS0160*/IOException) { }
        try { }
        catch { }
        /*error:CS1017*/catch (Exception) { }
        try { }
        catch (IOException) { }
        catch (Exception) { }
        try { }
        catch (Exception e) when (e.Message != null) { }
        catch (IOException) { }
        int w = /*error:CS0154*/W;
        W = 1;
        var c = new C { W = 2 };
        var n = nameof(W);
        Ref(ref /*error:CS0192*/r);
        Out(out /*error:CS0192*/r);
        In(in r);
        Ref(ref /*error:CS0199*/sr);
        int[] a = new int[2] /*error:CS0847*/{ 1, 2, 3 };
        int[,] b = new int[2, 2] { { 1, 2 }, /*error:CS0847*/{ 3 } };
        int[] d = new int[2] { 1, 2 };
        Console.WriteLine(w + a[0] + b[0, 0] + d[0] + c.r + n);
    }
    static C() { Ref(ref sr); }
    C() { Ref(ref r); Ref(ref this.r); }
    C(C other) { Ref(ref /*error:CS0192*/other.r); }
}
