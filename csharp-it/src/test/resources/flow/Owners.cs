using System;

class Owners
{
    bool b;

    int Property
    {
        get
        {
            if (b) return 1;
            return 2;
        }
    }

    int Expression() => 1;

    void Nested()
    {
        Func<int> lambda = () => { if (b) return 1; return 2; };
        Func<int> anonymous = delegate { return 1; };
        int Local() => 3;
        Console.WriteLine(lambda() + anonymous() + Local());
    }
}
