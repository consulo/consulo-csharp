// expectedOutput: 1
using System;

class Program
{
    delegate void D(in DateTime d);

    static void F(in DateTime x)
    {
        Console.WriteLine('1');
    }

    static void F(DateTime x)
    {
        Console.WriteLine('2');
    }

    static void Main()
    {
        D a = F;
        a(DateTime.MaxValue);
    }
}
