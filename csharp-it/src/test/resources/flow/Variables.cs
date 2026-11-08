class Variables
{
    bool b;
    object o;

    static bool Try(out int value)
    {
        value = 1;
        return true;
    }

    void Use(int value) { }

    void Declarations(int parameter)
    {
        int x;
        int y = parameter;
        x = y + 1;
        x++;
        Use(x);
    }

    void Conditions()
    {
        int x;
        if (b && Try(out x))
        {
            Use(x);
        }
        if (!(o is int i))
        {
            return;
        }
        Use(i);
    }

    void OutVar()
    {
        if (Try(out var y))
        {
            Use(y);
        }
    }

    void Tuple()
    {
        int x;
        int w;
        (x, w) = (1, 2);
    }

    void Catch()
    {
        int z;
        try
        {
            z = 1;
        }
        catch (System.Exception e)
        {
            z = 2;
        }
        Use(z);
    }
}
