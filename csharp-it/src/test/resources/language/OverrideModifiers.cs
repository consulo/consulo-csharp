class B
{
    public virtual int M() => 0;
    public virtual int P { get; set; }
    public virtual int Q { get; }
    public virtual int R { set { } }
    public int F;
    public virtual int Prop { get; }
    public virtual object O() => null;
    public virtual object OP { get; }
}
class D : B
{
    public sealed override int M() => 1;
    public override string ToString() => "";
}
class E : D
{
    public override int /*error:CS0239*/M() => 2;
    public override long /*error:CS1715*/P { get; set; }
    public override int Q { get; /*error:CS0546*/set; }
    public override int R { /*error:CS0545*/get => 0; }
    public override int /*error:CS0544*/F { get; }
    public override void /*error:CS0505*/Prop() { }
    public override string O() => null;
    public override string OP => null;
}
class E2 : B
{
    public override long /*error:CS0508*/M() => 2;
}
class G
{
    public abstract void /*error:CS0513*/A();
    private virtual void /*error:CS0621*/V() { }
    private abstract void /*error:CS0621*/W();
    virtual void /*error:CS0621*/V2() { }
    public static virtual void /*error:CS0112*/X() { }
    public sealed void /*error:CS0238*/Y() { }
    public abstract int Z { /*error:CS0513*/get; }
    protected virtual void Ok() { }
}
abstract class H
{
    public abstract void A();
    public abstract int Z { get; }
}
