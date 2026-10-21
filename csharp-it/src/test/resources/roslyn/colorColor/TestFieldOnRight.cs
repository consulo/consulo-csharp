class E
{
    public int F;
}

class F
{
    public E E;

    void M()
    {
        int f = /*@symbol:Field E F.E*/E/*@parent:Field int E.F*/.F;
    }
}
