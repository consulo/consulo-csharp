class E
{
    public event System.Action Event;
}

class F
{
    public E E;

    void M()
    {
        /*@symbol:Field E F.E*/E/*@parent:Event System.Action E.Event*/.Event += null;
    }
}
