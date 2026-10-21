using U = C;

class C
{
    void Instance(U U)
    {
        /*@symbol:Type C*/U/*@parent:Method void C.Static()*/.Static(); //Formerly CS7040 (bug)
    }

    static void Static()
    {
    }
}
