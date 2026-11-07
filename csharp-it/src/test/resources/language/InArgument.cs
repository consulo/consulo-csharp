// an argument of an in parameter can be passed with in - by reference, with the type of the value
class Test
{
    static int Read(in int value) => value;

    static int Named(in int first, in int second) => first + second;

    void Method(int value)
    {
        var /*type:int*/byValue = Read(value);
        var /*type:int*/literal = Read(1);
        var /*type:int*/byIn = Read(in value);
        var /*type:int*/named = Named(second: in value, first: 2);

        foreach (var /*type:int*/item in new int[0])
        {
        }
    }
}
