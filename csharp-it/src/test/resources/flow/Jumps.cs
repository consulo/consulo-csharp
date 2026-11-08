using System;
using System.Collections.Generic;

class Jumps
{
    bool b;

    void F() { }

    void Goto()
    {
        goto end;
        F();
    end:
        F();
    }

    void GotoBack()
    {
    again:
        F();
        if (b) goto again;
    }

    int Throw()
    {
        if (b) return 1;
        throw new Exception();
    }

    IEnumerable<int> Iterator()
    {
        yield return 1;
        if (b) yield break;
        F();
    }
}
