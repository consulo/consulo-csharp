class RefParameter
{
    static int Use(int value) => value;

    static long Use(long value) => value;

    void Method(ref int reference, out int output, in int input)
    {
        output = 1;
        var /*type:int*/copy = reference;
        var /*type:int*/result = Use(reference) + Use(output) + Use(input);
        Pass(ref reference);
    }

    static void Pass(ref int value)
    {
        value++;
    }
}
