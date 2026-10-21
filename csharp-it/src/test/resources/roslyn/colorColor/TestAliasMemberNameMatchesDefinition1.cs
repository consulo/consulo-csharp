using Q = E;

class E
{
    public void M(int x) { }
    public static void M(params int[] a) { }
}

class F
{
    public Q E { get; set; }

    void M()
    {
        /*@symbol:Property E F.E*/E/*@parent:Method void E.M(int x)*/.M(1);
    }
}
