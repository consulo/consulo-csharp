using System;

class E
{
    public void M(int x) { }
    public static void M(params int[] a) { }
}

class F
{
    void M()
    {
        var E = new E();
        Action<Action<int[]>> a = f =>
        {
            f = /*@symbol:Type E*/E/*@parent:Method void E.M(params int[] a)*/.M;
        };
    }
}
