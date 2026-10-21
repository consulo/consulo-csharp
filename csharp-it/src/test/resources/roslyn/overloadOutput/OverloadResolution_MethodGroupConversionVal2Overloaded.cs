// expectedOutput: 2
using System;

class Program
{
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
        Action<DateTime> a = F;
        a(DateTime.MaxValue);
    }
}
