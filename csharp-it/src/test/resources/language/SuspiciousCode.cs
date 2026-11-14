using System;
class /*warning:CS0659*/C
{
    /*error:CS0825*//*unresolved*/var f = 1;
    int x;
    sealed class S { protected int /*warning:CS0628*/P; protected void /*warning:CS0628*/M() { } protected internal int /*warning:CS0628*/Q; public override string ToString() => ""; }
    public override bool Equals(object o) => true;
    enum E { A }
    bool Use(object o) => o != null;
    void M(int a, bool b, C c, E en)
    {
        /*error:CS0819*/var p = 1, q = 2;
        const /*error:CS0822*/var k = 1;
        Use(p + q + k);
        /*warning:CS1717*/a = a;
        /*warning:CS1717*/x = x;
        /*warning:CS1717*/this.x = this.x;
        c.x = x;
        bool e = /*warning:CS1718*/a == a;
        bool g = /*warning:CS1718*/x != x;
        if (/*warning:CS0665*/b = true) { }
        while (/*warning:CS0665*/b = false) { }
        if (b = Use(c)) { }
        bool n = /*warning:CS0472*/a == null;
        bool m = /*warning:CS0472*/null != en;
        int? oa = null;
        bool o = oa == null;
        Use(e || g || n || m || o);
    }
}
