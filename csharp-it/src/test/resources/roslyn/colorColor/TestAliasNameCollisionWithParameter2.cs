using U = C;

class C
{
    void Instance(U U)
    {
        U.Instance(null);
        /*@symbol:Type C*/U/*@parent:Method void C.Static()*/.Static(); //Formerly CS7039 (bug)
    }

    static void Static()
    {
    }
}
