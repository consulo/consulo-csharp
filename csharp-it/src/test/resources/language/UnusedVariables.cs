// check:unused
using System;

class UnusedVariables
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

    void Constants()
    {
        int /*unused:CS0219*/reassigned = 1;
        reassigned = 2;
        int /*unused:CS0219*/assignedLater;
        assignedLater = 1;
        string /*unused:CS0219*/text = "a";
        int /*unused:CS0219*/expression = (1 + 2) * Constant;
        object /*unused:CS0219*/nothing = null;
        int? /*unused:CS0219*/nullable = null;
        Kind /*unused:CS0219*/kind = Kind.A;
        int /*unused:CS0219*/size = sizeof(int);
        string /*unused:CS0219*/name = nameof(UnusedVariables);
        int /*unused:CS0219*/fallback = default;
        int /*unused:CS0219*/first = 1, /*unused:CS0219*/second = 2;
        bool /*unused:CS0219*/flag = !true;
        const int /*unused:CS0219*/local = 1;
        int /*unused:CS0219*/captured = 1;
        Action action = () => captured = 2;
        for (int /*unused:CS0219*/i = 0; ; )
        {
            break;
        }
    }

    void Accepted()
    {
        int call = F();
        call = 1;
        var created = new object();
        int read = 1;
        Use(read);
        read = 2;
        int incremented = 1;
        incremented++;
        int compound = 1;
        compound += 2;
        int passed = 1;
        Try(out passed);
        int named = 1;
        string nameOf = nameof(named);
        Use(nameOf.Length);
        int chained = 1;
        chained = chained = 2;
        int conditional = F() > 0 ? 1 : 2;
        Type type = typeof(int);
    }
}
