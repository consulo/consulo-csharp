// check:unused
using System;

class /*unused:CC0009*/UnusedVariables
{
    const int Constant = 1;

    enum Kind { A }

    static bool Try(out int value)
    {
        value = 1;
        return true;
    }

    int F() => 1;

    void Use(int value) { }

    void /*unused:CC0008*/Constants()
    {
        int /*unused:CS0219*//*unused:CC0007*/reassigned = 1;
        /*unused:CC0007*/reassigned = 2;
        int /*unused:CS0219*/assignedLater;
        /*unused:CC0007*/assignedLater = 1;
        string /*unused:CS0219*//*unused:CC0007*/text = "a";
        int /*unused:CS0219*//*unused:CC0007*/expression = (1 + 2) * Constant;
        object /*unused:CS0219*//*unused:CC0007*/nothing = null;
        int? /*unused:CS0219*//*unused:CC0007*/nullable = null;
        Kind /*unused:CS0219*//*unused:CC0007*/kind = Kind.A;
        int /*unused:CS0219*//*unused:CC0007*/size = sizeof(int);
        int /*unused:CS0219*//*unused:CC0007*/fallback = default;
        int /*unused:CS0219*//*unused:CC0007*/first = 1, /*unused:CS0219*//*unused:CC0007*/second = 2;
        bool /*unused:CS0219*//*unused:CC0007*/flag = !true;
        const int /*unused:CS0219*//*unused:CC0007*/local = 1;
        int /*unused:CS0219*//*unused:CC0007*/captured = 1;
        Action /*unused:CC0007*/action = () => captured = 2;
        for (int /*unused:CS0219*//*unused:CC0007*/i = 0; ; )
        {
            break;
        }
    }

    void /*unused:CC0008*/NotConstant()
    {
        object /*unused:CC0007*/boxed = 1;
        int /*unused:CC0007*/call = F();
        /*unused:CC0007*/call = 1;
        var /*unused:CC0007*/created = new object();
        int read = 1;
        Use(read);
        /*unused:CC0007*/read = 2;
        int incremented = 1;
        /*unused:CC0007*/incremented++;
        int compound = 1;
        /*unused:CC0007*/compound += 2;
        int /*unused:CC0007*/passed = 1;
        Try(out /*unused:CC0007*/passed);
        int /*unused:CC0007*/named = 1;
        string nameOf = nameof(named);
        Use(nameOf.Length);
        int /*unused:CC0007*/chained = 1;
        /*unused:CC0007*/chained = /*unused:CC0007*/chained = 2;
        int /*unused:CC0007*/conditional = F() > 0 ? 1 : 2;
        Type /*unused:CC0007*/type = typeof(int);
    }
}
