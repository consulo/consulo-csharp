// expectedOutput: int\nlong\n
using System;

public class A
{
    const int C = 1;
 
    void AA(int x) { Console.WriteLine("int"); }
 
    public void AA(long x) { Console.Write("long"); }
 
    public void Goo() { A a = new A(); a.AA(C); }

    static void Main()
    {
        A a = new A();
        a.Goo();
        B.Goo();
    }
}

public class B
{
    const int C = 1;
    public static void Goo() { A a = new A(); a.AA(C);}
}

