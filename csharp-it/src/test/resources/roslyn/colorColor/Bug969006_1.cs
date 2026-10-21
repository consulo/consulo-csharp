enum E
{
    A
}

class C
{
    void M()
    {
        const E E = /*@symbol:Type E*/E.A;
        var z = /*@symbol:Local E E*/E;
    }
}
