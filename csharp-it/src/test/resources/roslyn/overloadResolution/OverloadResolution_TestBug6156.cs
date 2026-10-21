
class C
{
    public static void M()
    {
        int y = 123;
        Out2 o2 = new Out2();
        Ref2 r2 = o2;
        Out1 o1 = o2;
        Ref1 r1 = o2;

        r1.M(ref y); //-Ref1.M(ref int)
        o1.M(ref y); //-Ref1.M(ref int)
        o1.M(out y); //-Out1.M(out int)
        r2.M(ref y); //-Ref2.M(ref int)
        r2.M(out y); //-Out1.M(out int)
        o2.M(ref y); //-Ref2.M(ref int)
        o2.M(out y); //-Out2.M(out int)
    }
}
class Ref1
{
    public virtual void M(ref int x) { x = 1; } // SLOT1
}
class Out1 : Ref1
{
    public virtual void M(out int x) { x = 2; } // SLOT2
}
class Ref2 : Out1
{
    public override void M(ref int x) { x = 3; } 
    // CLR says this overrides SLOT2, even though there is a ref/out mismatch with Out1.
    // C# says this overrides SLOT1
}
class Out2 : Ref2
{
    public override void M(out int x) { x = 4; } 
    // CLR says this overrides SLOT2, even though there is a ref/out mismatch with Ref2.M.
    // C# says this overrides SLOT2
}