class C
{
    public class Inner
    {
        public static C M() { return null; }
    }
}

class F
{
    public C C;

    void M(C c)
    {
        M(/*@symbol:Type C*/C/*@parent:Type C.Inner*/.Inner.M());
    }
}
