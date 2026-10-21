using System;
using System.Collections.Generic;

public static class Program
{
    public static void Main()
    {
        var parameter = new List<string>();
        var result = parameter.FirstOrDefault(x => /*@type:System.String*/x. );
    }
}

public static class Enumerable
{
    public static TSource FirstOrDefault<TSource>(this IEnumerable<TSource> source, params TSource[] defaultValue)
    {
        return default(TSource);
    }

    public static TSource FirstOrDefault<TSource>(this IEnumerable<TSource> source, Func<TSource, bool> predicate, params TSource[] defaultValue)
    {
        return default(TSource);
}
}