class E
{
}

static class Extensions
{
    public static void M(this E e, int x) { }
    public static void M(this E e, params int[] a) { }
}

class F
{
    public E E;

    void M()
    {
        System.Action<int> f = /*@symbol:Field E F.E*/E/*@parent:Method void Extensions.M(this E e, int x)*/.M;
    }
}
