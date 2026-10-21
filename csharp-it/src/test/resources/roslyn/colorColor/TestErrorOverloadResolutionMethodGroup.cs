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
        System.Action<string> f = /*@symbol:Property E F.E*/E.M;
    }
}
