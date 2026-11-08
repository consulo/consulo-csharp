class Switch
{
    void F() { }

    void Sections(int i)
    {
        switch (i)
        {
            case 1:
            case 2:
                F();
                break;
            case 3:
                goto case 1;
            default:
                return;
        }
        F();
    }

    int Bool(bool b)
    {
        switch (b)
        {
            case true:
                return 1;
            case false:
                return 0;
        }
    }

    void Constant()
    {
        const int k = 2;
        switch (k)
        {
            case 1:
                F();
                break;
            case 2:
                goto default;
            default:
                break;
        }
    }

    void FallThrough(int i)
    {
        switch (i)
        {
            case 1:
                F();
            case 2:
                break;
        }
    }
}
