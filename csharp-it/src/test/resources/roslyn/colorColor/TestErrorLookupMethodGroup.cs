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
        System.Action<int> f = /*@symbol:Property E F.E*/E.Q;
    }
}
