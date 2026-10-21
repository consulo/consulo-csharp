class E
{
    public void M<T>(T x) { }
    public static void M<T>(params T[] a) { }
}

class F
{
    public E E;

    void M()
    {
        /*@symbol:Field E F.E*/E/*@parent:Method void E.M<T>(T x)*/.M<int>(1);
    }
}
