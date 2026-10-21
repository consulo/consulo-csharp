class E
{
    public void M(int x) { }
    public static void M(params int[] a) { }
}

class F
{
    public E E { get; set; }

    void M()
    {
        /*@symbol:Type E*/E/*@parent:Method void E.M(params int[] a)*/.M(1, 2);
        {
            E E;
        }
    }
}
