class E
{
    public void M(int x) { }
    public static void M(params int[] a) { }
}

delegate D D(E x);

class F
{
    void M()
    {
        /*@symbol:Type E*/E/*@parent:Method void E.M(params int[] a)*/.M(1, 2);
        D d = (E E) => null;
    }
}