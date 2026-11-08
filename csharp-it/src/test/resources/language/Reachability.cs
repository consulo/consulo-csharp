using System;
using System.Collections.Generic;

class Reachability
{
    bool b;

    static readonly bool ReadOnly = true;

    void F() { }

    void G() { }

    void AfterReturn()
    {
        return;
        /*warning:CS0162*/F();
        F();
    }

    void Declarations()
    {
        return;
        /*warning:CS0162*/int x;
    }

    void NotReported()
    {
        return;
        ;
        { }
        throw new Exception();
    }

    void InBlock()
    {
        return;
        {
            /*warning:CS0162*/F();
        }
    }

    void ConstantFalse()
    {
        if (false)
        {
            /*warning:CS0162*/F();
        }
        while (false) /*warning:CS0162*/F();
        for (int i = 0; false; i++) { /*warning:CS0162*/F(); }
        if (ReadOnly) F(); else G();
    }

    void ConstantTrue()
    {
        if (!false) return; else /*warning:CS0162*/F();
    }

    void ConstantExpression()
    {
        if (false || true) return;
        /*warning:CS0162*/F();
    }

    void AfterInfiniteLoop()
    {
        while (true) { }
        /*warning:CS0162*/F();
    }

    void LoopWithBreak()
    {
        while (true)
        {
            if (b) break;
        }
        F();
    }

    void Goto()
    {
        goto end;
        /*warning:CS0162*/F();
    end:
        F();
    }

    void UnreferencedLabel()
    {
        return;
        /*warning:CS0162*/end:
        F();
    }

    void Branches()
    {
        if (b) { return; /*warning:CS0162*/F(); } else { return; } /*warning:CS0162*/F();
    }

    void BothBranchesReported()
    {
        if (b) { return; /*warning:CS0162*/F(); } else { return; /*warning:CS0162*/G(); }
        F();
    }

    void TryFinally()
    {
        try
        {
            return;
        }
        finally
        {
            F();
        }
        /*warning:CS0162*/F();
    }

    void TryCatch()
    {
        try
        {
            return;
        }
        catch (Exception)
        {
        }
        F();
    }

    void BreakThroughFinally()
    {
        while (true)
        {
            try
            {
                break;
            }
            finally
            {
                F();
            }
        }
        F();
    }

    void ConstantSwitch()
    {
        const int k = 2;
        switch (k)
        {
            case 1:
                /*warning:CS0162*/F();
                break;
            default:
                F();
                break;
        }
    }

    IEnumerable<int> Iterator()
    {
        yield break;
        /*warning:CS0162*/F();
    }

    void Lambda()
    {
        Action action = () =>
        {
            return;
            /*warning:CS0162*/F();
        };
    }

    void LocalFunction()
    {
        return;
        void Local()
        {
        }
    }
}
