using Q = E;

class E
{
    public void M(int x) { }
    public static void M(params int[] a) { }
}

class F
{
    public E Q { get; set; }

    void M()
    {
        /*@symbol:Property E F.Q*/Q/*@parent:Method void E.M(int x)*/.M(1);
        {
            Q E;
            E Q;
        }
    }
}
