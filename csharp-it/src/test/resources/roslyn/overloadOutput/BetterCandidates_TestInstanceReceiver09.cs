// expectedOutput: 2
using System;
class Program
{
    static void Main()
    {
        var q = from x in new Q() select x;
    }
}
class Q
{
    public static object Select(Func<A, A> y)
    {
        Console.WriteLine(1);
        return null;
    }
    public object Select(Func<B, B> y)
    {
        Console.WriteLine(2);
        return null;
    }
}
class A {}
class B {}
