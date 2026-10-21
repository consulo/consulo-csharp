enum E
{
    Element,
}

class F
{
    public E E;

    void M(E e)
    {
        M(/*@symbol:Type E*/E/*@parent:Field E E.Element*/.Element);
    }
}
