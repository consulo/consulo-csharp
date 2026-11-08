using System;

struct Point
{
    public int X;
    public int Y;
}

class DefiniteAssignment
{
    bool b;
    object o;

    static bool Try(out int value)
    {
        value = 1;
        return true;
    }

    void Use(int value) { }

    void If()
    {
        int x;
        if (b) x = 1;
        Use(/*error:CS0165*/x);
    }

    void IfElse()
    {
        int x;
        if (b) x = 1;
        else x = 2;
        Use(x);
    }

    void TryCatch()
    {
        int z;
        try
        {
            z = 1;
        }
        catch
        {
        }
        Use(/*error:CS0165*/z);
    }

    void TryFinally()
    {
        int x;
        int y;
        try
        {
            x = 1;
        }
        finally
        {
            y = 2;
        }
        Use(x + y);
    }

    void NotPattern()
    {
        if (!(o is int i)) return;
        Use(i);
    }

    void NotPatternKeyword()
    {
        if (o is not int i) return;
        Use(i);
    }

    void Loop()
    {
        while (b)
        {
            int x;
            if (b) x = 1;
            Use(/*error:CS0165*/x);
        }
    }

    void Lambda()
    {
        int x;
        Action action = () => Use(/*error:CS0165*/x);
        int y;
        Func<int> assigned = () => { y = 1; return y; };
    }

    void LocalFunction()
    {
        int x;
        void Local() => Use(x);
        /*error:CS0165*/Local();
        int y;
        void Other() => Use(y);
        y = 1;
        Other();
    }

    void ShortCircuit()
    {
        int x;
        if (b && Try(out x)) Use(x);
        int y;
        if (b || !Try(out y)) return;
        Use(y);
        int z;
        if (b && false) Use(z);
    }

    void Conditional()
    {
        int x;
        Use(b ? (x = 1) : (x = 2));
        Use(x);
        int y;
        object v = o ?? (y = 1);
        Use(/*error:CS0165*/y);
    }

    void Operators()
    {
        int x;
        int y = /*error:CS0165*/x++;
        int z;
        /*error:CS0165*/z += 1;
        int w;
        w = /*error:CS0165*/w + 1;
        int n;
        Use(nameof(n).Length);
    }

    void Out()
    {
        int x;
        Try(out x);
        Use(x);
    }

    void Struct()
    {
        Point point;
        point.X = 1;
        point.Y = 2;
        Use(point.X);
    }

    void Switch()
    {
        int x;
        switch (o)
        {
            case int i:
                x = i;
                break;
            default:
                x = 0;
                break;
        }
        Use(x);
        int y;
        switch (o)
        {
            case int i:
                y = i;
                break;
        }
        Use(/*error:CS0165*/y);
    }

    void Tuple()
    {
        int x;
        int w;
        (x, w) = (1, 2);
        Use(x + w);
        int y;
        (y, w) = (/*error:CS0165*/y, 1);
    }

    void SwitchExpression(int k)
    {
        int y;
        var z = k switch { 1 => y = 1, _ => y = 2 };
        Use(y);
    }

    void Loops()
    {
        int x;
        while (true)
        {
            x = 1;
            break;
        }
        Use(x);
        int y;
        do
        {
            y = 1;
        }
        while (b);
        Use(y);
        int z;
        foreach (var i in new int[0]) z = i;
        Use(/*error:CS0165*/z);
    }

    void Goto()
    {
        int x;
    again:
        if (b)
        {
            x = 1;
            goto again;
        }
        Use(/*error:CS0165*/x);
    }

    void /*error:CS0177*/OutEnd(out int value)
    {
    }

    void OutReturn(out int value)
    {
        if (b) /*error:CS0177*/return;
        value = 1;
    }

    void OutRead(out int value)
    {
        Use(/*error:CS0269*/value);
        value = 1;
    }

    void /*error:CS0177*/OutCatch(out int value)
    {
        try
        {
            throw new Exception();
        }
        catch
        {
        }
    }

    void OutThrow(out int value)
    {
        throw new Exception();
    }

    void OutFinally(out int value)
    {
        try
        {
            return;
        }
        finally
        {
            value = 1;
        }
    }
}
