using System;

class JumpErrors
{
    bool b;

    enum Kind
    {
        A,
        B,
        C = 5,
        D
    }

    const int One = 1;

    void Use(int value) { }

    void Break()
    {
        /*error:CS0139*/break;
    }

    void Continue(int i)
    {
        /*error:CS0139*/continue;
    }

    void ContinueInSwitch(int i)
    {
        switch (i)
        {
            /*error:CS8070*/case 1:
                /*error:CS0139*/continue;
        }
    }

    void InLambda()
    {
        while (b)
        {
            Action action = () => { /*error:CS1632*/break; };
            if (b) break;
        }
    }

    void InLocalFunction()
    {
        while (b)
        {
            void Local()
            {
                /*error:CS0139*/continue;
            }
            Local();
        }
    }

    void Accepted(int i)
    {
        while (b)
        {
            switch (i)
            {
                case 1:
                    continue;
                default:
                    break;
            }
        }
        foreach (var item in new int[0])
        {
            Action action = () =>
            {
                for (int j = 0; j < 2; j++)
                {
                    if (b) break;
                    continue;
                }
            };
            break;
        }
    }

    void Labels(int i)
    {
        goto /*unresolved*//*error:CS0159*/missing;
    }

    void NestedLabel(int i)
    {
        {
            inner:
            Use(i);
        }
        goto /*unresolved*//*error:CS0159*/inner;
    }

    void GotoCase(int i)
    {
        switch (i)
        {
            /*error:CS0163*/case 1:
                /*error:CS0159*/goto case 2;
            case 3:
                goto case 1;
            /*error:CS8070*/case 4:
                /*error:CS0159*/goto default;
        }
    }

    void Rethrow()
    {
        /*error:CS0156*/throw;
    }

    void RethrowInLambda()
    {
        try
        {
            Use(1);
        }
        catch
        {
            Action action = () => { /*error:CS0156*/throw; };
            throw;
        }
    }

    void RethrowInFinally()
    {
        try
        {
            Use(1);
        }
        catch
        {
            try
            {
                Use(2);
            }
            finally
            {
                /*error:CS0724*/throw;
            }
        }
    }

    void RethrowAccepted()
    {
        try
        {
            Use(1);
        }
        catch
        {
            try
            {
                throw;
            }
            catch
            {
                throw;
            }
        }
    }

    void Duplicates(int i, string s, Kind kind)
    {
        switch (i)
        {
            case 1:
                break;
            /*error:CS0152*/case 1:
                break;
            case 2:
            case 3:
            /*error:CS0152*/case One:
                break;
            default:
                break;
            /*error:CS0152*/default:
                break;
        }
        switch (s)
        {
            case "a":
                break;
            /*error:CS0152*/case "a":
                break;
        }
        switch (kind)
        {
            case Kind.A:
            case Kind.B:
                break;
            case Kind.D:
                break;
            /*error:CS0152*/case Kind.A:
                break;
        }
    }
}
