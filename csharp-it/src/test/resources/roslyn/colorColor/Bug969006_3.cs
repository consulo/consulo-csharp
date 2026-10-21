enum E
{
    A
}

class C
{
    void M()
    {
        E E = /*@symbol:Type E*/E.A;
        var z = /*@symbol:Local E E*/E;
    }
}
