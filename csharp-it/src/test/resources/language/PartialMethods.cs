partial class C
{
    partial void A();
    partial void /*error:CS0111*//*error:CS0756*/A();
    partial void /*error:CS0759*/B() { }
    partial void /*error:CS0111*//*error:CS0757*/B() { }
    partial void D(int x);
    partial void D(int y) { }
    static partial void E();
    partial void /*error:CS0763*/E() { }
    partial void F<T>() where T : class;
    partial void /*error:CS0761*/F<T>() where T : struct { }
    partial void /*error:CS8797*/G(out int x);
    partial int /*error:CS8796*/H();
    public partial void I();
    public partial void I() { }
    public partial int /*error:CS8795*/J();
    partial void K(params int[] a);
    partial void /*error:CS0758*/K(int[] a) { }
    partial void /*error:CS0759*/L() { }
    public partial void M();
    internal partial void /*error:CS8799*/M() { }
    public virtual partial void N();
    public partial void /*error:CS8800*/N() { }
    public partial int R();
    public partial long /*error:CS8817*/R() => 0;
    partial void Q(ref int x);
    partial void /*error:CS0759*/Q(in int x) { }
    partial void S<T>(T t);
    partial void S<U>(U u) { }
}
class O
{
    partial void /*error:CS0751*/P();
}
