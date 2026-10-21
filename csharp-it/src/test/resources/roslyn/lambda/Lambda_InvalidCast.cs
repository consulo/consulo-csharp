using System;
#nullable enable
internal class Program
{
    void Main(string[] args)
    {
        Choice(args.Length > 0
            ? /*@type:System.Action*/(Action)(() => DS1()
            : () => DS2(args[0]));
    }

    void DS1()
    { }

    void DS2(string a)
    { }

    void Choice(Action a)
    {
        a();
    }
}