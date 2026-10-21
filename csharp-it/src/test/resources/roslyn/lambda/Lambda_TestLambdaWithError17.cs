using System;

class Program
{
    static void Main(string[] args)
    {
        Ma(action: (x, y) => /*@type:System.String*/x.ToString(), t: string.Empty);
        Mb(action: (x, y) => /*@type:System.String*/x.ToString(), t: string.Empty);
    }
    static void Ma<T>(T t, Action<T, T, int> action) { }
    static void Mb<T>(T t, Action<T, T, int> action) { }
    static void Mb() { }
}
