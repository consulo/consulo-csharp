using System;
using System.Collections.Generic;
using System.Threading.Tasks;

class ReturnPaths
{
    bool b;

    void F() { }

    int /*error:CS0161*/If()
    {
        if (b) return 1;
    }

    int /*error:CS0161*/Empty()
    {
    }

    int /*error:CS0161*/While()
    {
        while (b) { }
    }

    int /*error:CS0161*/TryCatch()
    {
        try
        {
            return 1;
        }
        catch
        {
            F();
        }
    }

    int /*error:CS0161*/NotConstant()
    {
        bool c = true;
        while (c) { }
    }

    int Property
    {
        /*error:CS0161*/get
        {
            if (b) return 1;
        }
    }

    int this[int i]
    {
        /*error:CS0161*/get
        {
            if (b) return i;
        }
    }

    public static int operator /*error:CS0161*/+(ReturnPaths a, ReturnPaths c)
    {
        if (a.b) return 1;
    }

    async Task<int> /*error:CS0161*/Async()
    {
        await Task.Yield();
        if (b) return 1;
    }

    int Accepted(int i)
    {
        if (b) return 1;
        else return 2;
    }

    int WhileTrue()
    {
        while (true) { }
    }

    int WhileTrueBreak()
    {
        while (true)
        {
            if (b) break;
        }
        return 1;
    }

    int ForEver()
    {
        for (;;) { }
    }

    int Throws()
    {
        throw new Exception();
    }

    int TryFinally()
    {
        try
        {
            return 1;
        }
        finally
        {
            F();
        }
    }

    int Constant()
    {
        const bool always = true;
        if (always) return 1;
    }

    int Goto()
    {
    again:
        F();
        goto again;
    }

    int Switch(int i)
    {
        switch (i)
        {
            case 1:
                return 1;
            default:
                return 2;
        }
    }

    int SwitchBool(bool c)
    {
        switch (c)
        {
            case true:
                return 1;
            case false:
                return 2;
        }
    }

    int SwitchType(int i)
    {
        switch (i)
        {
            case int x:
                return x;
        }
    }

    int SwitchVar(object o)
    {
        switch (o)
        {
            case string s:
                return s.Length;
            case var x:
                return x.GetHashCode();
        }
    }

    int SwitchDiscard(string text)
    {
        switch (text)
        {
            case var /*type:string*/value when value.Length > 0:
                return value.Length;
            case var _:
                return 0;
        }
    }

    int /*error:CS0161*/SwitchVarWhen(object o)
    {
        switch (o)
        {
            case var x when x != null:
                return 1;
        }
    }

    int /*error:CS0161*/SwitchNullable(int? i)
    {
        switch (i)
        {
            case int x:
                return x;
        }
    }

    int Expression() => 1;

    async Task NoValue()
    {
        await Task.Yield();
        if (b) return;
    }

    IEnumerable<int> Iterator()
    {
        if (b) yield return 1;
    }

    void Lambdas()
    {
        Func<int> lambda = () /*error:CS1643*/=> { if (b) return 1; };
        Func<int> anonymous = /*error:CS1643*/delegate { if (b) return 1; };
        Func<int, int> parameter = x /*error:CS1643*/=> { if (b) return x; };
        Func<Task<int>> async = async () /*error:CS1643*/=> { await Task.Yield(); if (b) return 1; };
        Func<int> throws = () => { throw new Exception(); };
        Func<int> accepted = () => { if (b) return 1; return 2; };
        Action action = () => { if (b) return; };
        Func<Task> task = async () => { await Task.Yield(); if (b) return; };
        int /*error:CS0161*/Local()
        {
            if (b) return 1;
        }
    }
}
