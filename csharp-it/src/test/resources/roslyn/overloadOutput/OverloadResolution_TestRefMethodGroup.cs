// expectedOutput: pass\npass
using System;

class Program
{
    static void M()
    {
        Console.WriteLine("pass");
    }

    public static void Main(string[] args)
    {
        Action a1 = new Action(ref M);
        a1();
        Action a2 = new Action(out a1);
        a2();
    }
}