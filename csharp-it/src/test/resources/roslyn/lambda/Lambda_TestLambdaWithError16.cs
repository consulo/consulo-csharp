using System;

class Program
{
    static void Main(string[] args)
    {
        Thing<string> t = null;
        t.X1<string>(x => /*@type:System.String*/x, 1); // too many args
        t.X2<string>(x => /*@type:System.String*/x);    // too few args
        t.M2<string>(string.Empty, x => /*@type:System.String*/x, 1); // too many args
        t.M3<string>(string.Empty, x => /*@type:System.String*/x); // too few args
    }
}
public class Thing<T>
{
    public void M2<T>(T x, Func<T, T> func) {}
    public void M3<T>(T x, Func<T, T> func, T y) {}
}
public static class XThing
{
    public static Thing<T> X1<T>(this Thing<T> self, Func<T, T> func) => null;
    public static Thing<T> X2<T>(this Thing<T> self, Func<T, T> func, int i) => null;
}
