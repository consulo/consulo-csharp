delegate void E();

class F
{
    public event E E { add { } remove { } }

    void M()
    {
        E/*@parent:Method void E.Invoke()*/.Invoke();
    }
}
