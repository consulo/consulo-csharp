using System.Collections.Generic;

class Loops
{
    bool b;

    void F() { }

    void While()
    {
        while (b)
        {
            if (b) continue;
            F();
        }
        F();
    }

    int WhileTrue()
    {
        while (true)
        {
            if (b) break;
        }
        return 1;
    }

    void Do()
    {
        do
        {
            if (b) break;
            F();
        }
        while (b);
    }

    void For()
    {
        for (int i = 0; i < 10; i++)
        {
            if (b) continue;
            F();
        }
    }

    int ForEver()
    {
        for (;;)
        {
            F();
        }
    }

    void Foreach(List<int> list)
    {
        foreach (int i in list)
        {
            if (i == 0) break;
        }
        F();
    }
}
