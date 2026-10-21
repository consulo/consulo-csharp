using System;

class Program
{
    static void Main(string[] args)
    {
        Ma(string.Empty, (x, y) => /*@type:System.String*/x.ToString());
        Mb(string.Empty, (x, y) => /*@type:System.String*/x.ToString());
    }
    static void Ma<T>(T t, Action<T, T, int> action) { }
    static void Mb<T>(T t, Action<T, T, int> action) { }
    static void Mb() { }
}
