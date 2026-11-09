// check:unused
using System;
using System.Runtime.InteropServices;

class /*unused:CC0009*/FieldUsage
{
    private int /*unused:CS0169*/unused;
    private int /*warning:CS0414*/initialized = 1;
    private int /*warning:CS0414*/assigned;
    private int incremented;
    private int compound;
    private int /*warning:CS0649*/read;
    private int /*warning:CS0649*/neverAssigned;
    private int passedOut;
    private int passedRef;
    private int /*warning:CS0414*/chained;
    private int chainedOuter;
    private int /*warning:CS0414*/viaOther;
    private readonly int /*warning:CS0414*/readonlyInitialized = 2;
    private static int /*warning:CS0414*/shared;
    private int /*warning:CS0649*/onlyNameOf;
    private int nonConstant;
    private int tupleFirst;
    private int tupleSecond;
    private string /*warning:CS0649*/text;
    private bool /*warning:CS0649*/flag;
    private object /*warning:CS0649*/reference;
    private int[] /*warning:CS0649*/array;
    private int? /*warning:CS0649*/nullable;
    private Kind /*warning:CS0649*/kind;
    private int /*warning:CS0414*/defaulted = default;
    private int initializedRead = 3;
    [Obsolete]
    private int /*warning:CS0649*/attributed;
    private int /*warning:CS0414*/initializerObject;
    private object boxed = 1;
    private Wrapper converted = 1;

    class Wrapper
    {
        public static implicit operator Wrapper(int value) => new Wrapper();
    }

    enum Kind { A }

    static void Out(out int value) { value = 1; }

    static void Ref(ref int value) { }

    void Use(object value) { }

    int Get() => 1;

    void /*unused:CC0008*/Method(FieldUsage other)
    {
        assigned = 1;
        incremented++;
        compound += 1;
        Use(read);
        Use(neverAssigned);
        Out(out passedOut);
        Ref(ref passedRef);
        chainedOuter = chained = 2;
        other.viaOther = 3;
        shared = 4;
        Use(nameof(onlyNameOf));
        nonConstant = Get();
        (tupleFirst, tupleSecond) = (1, 2);
        Use(text + flag + reference + array + nullable + kind + initializedRead + attributed);
        var created = new FieldUsage { initializerObject = 5 };
        Use(created);
    }
}

[StructLayout(LayoutKind.Sequential)]
class /*unused:CC0009*/Layout
{
    private int unused;
    private int assigned;

    void Method()
    {
        assigned = 1;
    }
}

partial class /*unused:CC0009*/Parts
{
    private int /*warning:CS0414*/assigned;
}

partial class Parts
{
    void /*unused:CC0008*/Method()
    {
        assigned = 1;
    }
}
