// expectedOutput: PASS\nPASS
using System;

class A
{
    private A(int x) { }
    public A(long x)
    {
        Console.WriteLine("PASS");
    }
 
    private void M(int x) { }
    public void M(long x)
    {
        Console.WriteLine("PASS");
    }
}
 
class B: A
{
    public B(): base(123)
    {
        base.M(123);
    }

    public static void Main()
    {
        var unused = new B();
    }
}
