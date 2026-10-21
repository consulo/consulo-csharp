delegate void E(int x);

class F
{
    public event E E;

    void M()
    {
        /*@symbol:Event E F.E*/E/*@parent:Method void E.Invoke(int x)*/.Invoke(1);
    }
}
