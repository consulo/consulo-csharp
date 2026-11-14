using System;
using System.Threading.Tasks;
static class Ext
{
    public static void A(ref this int x) { }
    public static void B(this ref int x) { }
    public static void C(out /*error:CS8328*/this int x) { x = 0; }
    public static void F(params /*error:CS1104*/this int[] a) { }
    public static void G(ref string s) { }
    public static void /*error:CS8337*/H(this ref string s) { }
    public static void I(this in int x) { }
    static class Nested { public static void /*error:CS1109*/N(this int x) { } }
}
class P
{
    void A(ref /*error:CS8328*/params int[] a) { }
    void B(out /*error:CS8328*/params int[] a) { a = null; }
    void C(/*error:CS0225*/params int a) { }
    void C2(/*error:CS0225*/params int[,] a) { }
    void C3(params int[] a) { }
    async Task D(ref int /*error:CS1988*/x) { await Task.Yield(); }
    async Task E(out int /*error:CS1988*/x) { await Task.Yield(); x = 0; }
    async Task E2(int x) { await Task.Yield(); }
    void F(int /*error:CS1750*/x = "s") { }
    void F2(byte b = 1, string s = null, int? n = null, object o = null, double d = 1) { }
}
