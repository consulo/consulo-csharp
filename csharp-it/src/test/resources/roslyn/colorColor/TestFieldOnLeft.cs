class E
{
    public void M(int x) { }
    public static void M(params int[] a) { }
}

class F
{
    public E E;

    void M()
    {
        /*@symbol:Field E F.E*/E/*@parent:Method void E.M(int x)*/.M(1);
    }
}
