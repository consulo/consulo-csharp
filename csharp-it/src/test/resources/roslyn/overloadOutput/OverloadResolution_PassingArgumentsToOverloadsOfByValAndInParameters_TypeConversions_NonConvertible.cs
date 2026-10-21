// expectedOutput: val
using System;
using System.Text;
class Program
{
    static void M(string x) { Console.WriteLine("val"); }
    static void M(in StringBuilder x) { Console.WriteLine("in"); }

    static void Main()
    {
        M(null);
    }
}