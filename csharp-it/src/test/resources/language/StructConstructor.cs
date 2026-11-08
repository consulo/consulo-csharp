// csharp:9.0
using System;

struct Point
{
    public int X;
    public int Y;
    public static int Shared;
    public const int Origin = 0;

    public int Auto { get; set; }

    public int Computed
    {
        get { return 0; }
        set { }
    }

    public event Action Changed;

    public Point(int x)
    {
        this.X = x;
        Y = 1;
        Auto = 2;
        Changed = null;
    }

    public Point(long value)
    {
        this = default;
    }

    public Point(short value) : this(1)
    {
    }

    public /*error:CS0171*//*error:CS0843*//*error:CS0171*/Point(byte value)
    {
        X = 1;
    }

    public Point(sbyte value)
    {
        X = 1;
        if (value > 0)
        {
            /*error:CS0171*//*error:CS0843*//*error:CS0171*/return;
        }
        Y = 1;
        Auto = 1;
        Changed = null;
    }

    public Point(char value)
    {
        X = 1;
        Y = 2;
        /*error:CS0188*/Computed = 3;
        Auto = 4;
        Changed = null;
    }

    public Point(ushort value)
    {
        X = 1;
        Y = 2;
        Pass(/*error:CS0188*/this);
        Auto = 3;
        Changed = null;
    }

    public Point(double value)
    {
        Init(out X);
        Y = 2;
        Auto = 3;
        Changed = null;
        Shared = 4;
    }

    public Point(decimal value)
    {
        X = 1;
        Y = 2;
        Changed = null;
        Auto = /*error:CS0188*/Get();
    }

    public Point(string value)
    {
        throw new Exception();
    }

    public Point(object value)
    {
        X = 1;
        Y = 2;
        Auto = 3;
        Changed = null;
        Action action = () => { };
        Use(X + Computed + Get());
    }

    public Point(int a, int b)
    {
        X = /*error:CS9015*/Y;
        Y = 1;
        Auto = 2;
        Changed = null;
    }

    public Point(int a, long b)
    {
        X = /*error:CS9014*/Auto;
        Y = 1;
        Auto = 2;
        Changed = null;
    }

    public Point(int a, short b)
    {
        /*error:CS9015*/this.Y += 1;
        X = 1;
        Auto = 2;
        Changed = null;
    }

    static void Pass(Point point) { }

    static void Init(out int value)
    {
        value = 1;
    }

    static void Use(int value) { }

    int Get() => 1;
}

struct Initialized
{
    public int A;

    public int B;

    public /*error:CS0171*/Initialized(int value)
    {
        A = value;
    }
}
