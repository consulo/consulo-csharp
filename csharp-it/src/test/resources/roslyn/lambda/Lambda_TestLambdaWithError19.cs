using System;
using System.Linq.Expressions;

class Program
{
    static void Main(string[] args)
    {
        Ma(string.Empty, (x, y) => /*@type:System.String*/x.ToString());
        Mb(string.Empty, (x, y) => /*@type:System.String*/x.ToString());
        Mc(string.Empty, (x, y) => /*@type:System.String*/x.ToString());
    }
    static void Ma<T>(T t, Expression<Action<T, T, int>> action) { }
    static void Mb<T>(T t, Expression<Action<T, T, int>> action) { }
    static void Mb<T>(T t, Action<T, T, int> action) { }
    static void Mc<T>(T t, Expression<Action<T, T, int>> action) { }
    static void Mc() { }
}
