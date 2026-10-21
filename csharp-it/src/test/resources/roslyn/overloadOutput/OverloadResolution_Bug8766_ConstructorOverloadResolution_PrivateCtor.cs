// expectedOutput: int\nint\nlong\n
using System;
 
public class A
{
    const int C = 1;
 
    private A(int x) { Console.WriteLine("int"); }
 
    public A(long x) { Console.WriteLine("long"); }
 
    public void Goo() { A a = new A(C); }
    
    static void Main()
    {
        A a = new A(C);
        a.Goo();
        B.Goo();
    }
}

public class B
{
    const int C = 1;
    public static void Goo() { A a = new A(C);}
}
