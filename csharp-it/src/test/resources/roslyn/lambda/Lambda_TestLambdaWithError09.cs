using System;

public static class Program
{
    public static void Main()
    {
        var parameter = new MyList<string>();
        var result = parameter.FirstOrDefault(x => /*@type:System.String*/x. );
    }
}

public class MyList<TSource>
{
    public TSource FirstOrDefault(TSource defaultValue)
    {
        return default(TSource);
    }

    public TSource FirstOrDefault(Func<TSource, bool> predicate, TSource defaultValue)
    {
        return default(TSource);
    }
}
