class Branches
{
    bool b;

    void F() { }

    void Sequence()
    {
        F();
        int x = 1;
        ;
        { F(); }
    }

    void IfElse()
    {
        if (b)
            F();
        else
            x();
        F();
    }

    void x() { }

    void IfWithoutElse()
    {
        if (b)
        {
            return;
        }
        F();
    }

    void ConstantFalse()
    {
        if (false)
        {
            F();
        }
        F();
    }

    void ConstantNot()
    {
        if (!false) return; else F();
    }

    int ConstantTrue()
    {
        const bool always = true;
        if (!!always)
        {
            return 1;
        }
        else
        {
            F();
        }
    }
}
