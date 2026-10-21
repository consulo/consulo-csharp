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
        /*@symbol:Local E E*/E/*@parent:Method void E.M(int x)*/.M(1);
    }
}
