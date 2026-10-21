class E
{
    public void M(int x) { }
    public static void M(params int[] a) { }
}

class F
{
    void M()
    {
        E E = null;
        /*@symbol:Type E*/E/*@parent:Method void E.M(params int[] a)*/.M(1, 2);
    }
}
