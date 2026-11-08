class TupleAssignment
{
    void Method()
    {
        int x;
        string y;
        (x, y) = (1, "");
        var /*type:int*/first = x;
        var /*type:string*/second = y;
        (int a, string b) = (2, "");
        var /*type:int*/third = a;
        var /*type:string*/fourth = b;
    }
}
