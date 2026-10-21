class E
{
    public int P { get; set; }
}

class F
{
    public E E;

    void M()
    {
        int f = /*@symbol:Field E F.E*/E/*@parent:Property int E.P*/.P;
    }
}
