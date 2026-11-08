class SwitchFallThrough
{
    bool b;

    void F() { }

    void Sections(int i)
    {
        switch (i)
        {
            /*error:CS0163*/case 1:
                F();
            case 2:
                break;
        }
        switch (i)
        {
            case 1:
            /*error:CS0163*/case 2:
                if (b) break;
            case 3:
                break;
        }
        switch (i)
        {
            /*error:CS8070*/case 1:
                F();
        }
        switch (i)
        {
            case 1:
                break;
            /*error:CS8070*/default:
        }
    }

    void Accepted(int i)
    {
        switch (i)
        {
            case 1:
                F();
                break;
            case 2:
            {
                return;
            }
            case 3:
                goto case 1;
            case 4:
                goto default;
            case 5:
                if (b) break;
                else return;
            case 6:
                while (true) { }
            case 7:
                throw new System.Exception();
            default:
                F();
                break;
        }
    }
}
