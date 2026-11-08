using System;

class Try
{
    bool b;

    void F() { }

    void Catch()
    {
        try
        {
            F();
        }
        catch (Exception e)
        {
            return;
        }
        F();
    }

    void ReturnThroughFinally()
    {
        try
        {
            return;
        }
        finally
        {
            F();
        }
        F();
    }

    void BreakThroughFinally()
    {
        while (true)
        {
            try
            {
                if (b) break;
            }
            finally
            {
                F();
            }
        }
        F();
    }

    void GotoThroughFinally()
    {
        try
        {
            goto end;
        }
        finally
        {
            F();
        }
    end:
        F();
    }
}
