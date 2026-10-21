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
        /*@symbol:Type E*/Q/*@parent:Method void E.M(params int[] a)*/.M(1, 2);
        {
            Q E;
            E Q;
        }
    }
}
