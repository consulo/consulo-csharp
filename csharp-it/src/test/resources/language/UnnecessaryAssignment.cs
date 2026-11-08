// check:unused
using System;

class UnnecessaryAssignment
{
    bool b;
    object o;

    int F() => 1;

    void Use(int value)
    {
        Console.Write(value);
    }

    static bool Try(out int value)
    {
        value = 1;
        return true;
    }

    void Overwritten()
    {
        int /*unused:CC0007*/x = F();
        x = F();
        Use(x);
    }

    void NeverRead()
    {
        int /*unused:CC0007*/x = F();
        /*unused:CC0007*/x = 2;
    }

    void Last()
    {
        int x = F();
        Use(x);
        /*unused:CC0007*/x = 2;
    }

    void Increment()
    {
        int x = F();
        /*unused:CC0007*/x++;
        int y = F();
        /*unused:CC0007*/y += F();
    }

    void Out()
    {
        int x;
        Try(out /*unused:CC0007*/x);
        Try(out var /*unused:CC0007*/y);
    }

    void Patterns()
    {
        if (o is int /*unused:CC0007*/i)
        {
        }
        (int first, int /*unused:CC0007*/second) = (1, 2);
        Use(first);
    }

    void Loop()
    {
        int /*unused:CC0007*/x = F();
        for (int i = 0; i < 3; i++)
        {
            /*unused:CC0007*/x = i;
        }
    }

    void TryFinally()
    {
        int /*unused:CC0007*/x = F();
        try
        {
            x = F();
            Use(x);
        }
        finally
        {
        }
    }

    void Accepted(int parameter)
    {
        int branch = F();
        if (b) branch = F();
        Use(branch);
        int loop = 0;
        while (b)
        {
            Use(loop);
            loop = F();
        }
        int caught = F();
        try
        {
            caught = F();
        }
        catch
        {
        }
        Use(caught);
        int self = F();
        self = self + 1;
        Use(self);
        parameter = F();
        Use(parameter);
        int reference = F();
        Pass(ref reference);
        int captured = F();
        Action action = () => Use(captured);
        action();
    }

    static void Pass(ref int value)
    {
        value++;
    }
}
