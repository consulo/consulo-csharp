// check:unused
using System;

[Foo]
public class Root : IUsed
{
    private UsedByField /*warning:CS0649*/field;
    private Generic<int> /*warning:CS0649*/generic;

    public object Run()
    {
        Use(field, generic);
        return new UsedByNew();
    }

    private void Use(object a, object b) { }

    private class /*unused:CC0009*/PrivateUnused { }

    internal class /*unused:CC0009*/InternalNested { }

    public class PublicNested { }

    protected class ProtectedNested { }
}

internal class /*unused:CC0009*/Unused { }

class /*unused:CC0009*/DefaultInternal { }

public class Public { }

internal class UsedByField { }

internal class UsedByNew { }

internal class Generic<T> { }

internal class /*unused:CC0009*/SelfOnly
{
    private SelfOnly /*warning:CS0414*/next;

    internal static SelfOnly Create() => new SelfOnly { next = null };
}

internal static class Extensions
{
    public static void Twice(this int value) { }
}

internal class WithMain
{
    private static void Main() { }
}

internal class FooAttribute : Attribute { }

internal class /*unused:CC0009*/UnusedAttribute : Attribute { }

internal enum /*unused:CC0009*/UnusedEnum { A }

internal interface /*unused:CC0009*/IUnused { }

internal interface IUsed { }

internal partial class /*unused:CC0009*/Parts { }

internal partial class Parts { }
