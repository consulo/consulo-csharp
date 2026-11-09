using System;
using System.Collections.Generic;

internal class I { }
internal interface II { I M(); }

public delegate I /*error:CS0058*/D1();
public delegate void /*error:CS0059*/D2(I i);
public class /*error:CS0060*/Pub : I { }
public interface /*error:CS0061*/IPub : II { }
public interface IMembers { I /*error:CS0050*/M(); I /*error:CS0053*/P { get; } }

public class C
{
    public I /*error:CS0050*/M1() => null;
    public void /*error:CS0051*/M2(I i) { }
    public I /*error:CS0052*/F;
    public I /*error:CS0052*/A, /*error:CS0052*/B;
    public I /*error:CS0053*/P { get; set; }
    public I /*error:CS0054*/this[int x] => null;
    public int /*error:CS0055*/this[I x] => 0;
    public static I operator /*error:CS0056*/+(C a, C b) => null;
    public static C operator /*error:CS0057*/-(C a, I b) => null;
    public static implicit operator /*error:CS0056*/I(C c) => null;
    public static explicit operator /*error:CS0057*/C(I c) => null;
    public List<I> /*error:CS0050*/G() => null;
    public I[] /*error:CS0050*/Array() => null;
    internal I Ok() => null;
    protected I /*error:CS0050*/Prot() => null;
    private I Private() => null;
    private class Nested { }
    public Nested /*error:CS0050*/N() => null;
    protected class PN { }
    protected PN Pn() => null;
    internal PN /*error:CS0050*/Ipn() => null;
    protected internal I /*error:CS0050*/Pi() => null;
    public event Action<I> /*error:CS7025*/E;
    public event Action<I> /*error:CS7025*/CustomE { add { } remove { } }
    public /*error:CS0051*/C(I i) { }
    internal C(I i, int x) { }
    public (I, int) /*error:CS0050*/Tuple() => default;
    public Func<List<I[]>> /*error:CS0050*/Func() => null;
    public I[] /*error:CS0054*//*error:CS0055*/this[I a, int b] => null;

    public void Body()
    {
        I local = null;
        I Local(I i) => i;
        Func<I, I> lambda = i => i;
        Local(local);
        lambda(local);
    }
}

internal class Hidden
{
    public I M() => null;
    public class Inner { public I F; }
}

public class Outer
{
    internal class In { }
    public In /*error:CS0050*/Get() => null;
    public class Nest { public In /*error:CS0050*/X() => null; }
    private delegate I PD();
    public delegate I /*error:CS0058*/ND();
}

public class Base
{
    protected class Prot { }
    protected internal class PI { }
    private protected class PP { }
    private class Priv { }
    protected Prot A() => null;
    public Prot /*error:CS0050*/B() => null;
    protected PI C() => null;
    public PI /*error:CS0050*/D() => null;
    protected PP /*error:CS0050*/E() => null;
    private protected PP F() => null;
    internal PP /*error:CS0050*/G() => null;
    internal Prot /*error:CS0050*/H() => null;
    protected internal Prot /*error:CS0050*/J() => null;
    protected internal PI K() => null;
    private class Private { public Priv X() => null; public Prot Y() => null; }
}

public class Derived : Base
{
    protected Prot Q() => null;
    public class N { protected Prot /*error:CS0050*/R() => null; }
}

public class Other { protected Base.PI /*error:CS0050*/S() => null; }
public class Impl : II { I II.M() => null; }
public class /*error:CS0060*/Gen : List<I> { }
public struct S : II { public I /*error:CS0050*/M() => null; }
public enum En { A }
public class Fields { public const En E = En.A; public static readonly En[] All = null; public En Prop { get; set; } }

public partial class Part { }
partial class Part { public I /*error:CS0050*/M() => null; }

