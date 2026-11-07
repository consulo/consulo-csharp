// a lambda converts to Expression<TDelegate> as to TDelegate - the expression tree of it
using System;
using System.Linq.Expressions;

public class Test
{
    public static void Method()
    {
        Take(() => "test");
        var /*type:string*/text = Overloaded(() => "test");

        Expression<Func<int, bool>> /*type:System.Linq.Expressions.Expression<System.Func<int, bool>>*/positive = value => value > 0;
        Expression<Func<string, int>> length = /*type:string*/value => value.Length;
        Func<int, bool> /*type:System.Func<int, bool>*/compiled = positive.Compile();
        var /*type:bool*/result = positive.Compile()(1);
        var /*type:System.Linq.Expressions.Expression*/body = positive.Body;
        var /*type:int*/inferred = Infer(value => value.Length);
    }

    public static void Take(Expression<Func<string>> expression)
    {
    }

    // an expression tree of a lambda returning string is better than a delegate returning object
    public static string Overloaded(Expression<Func<string>> expression) => "";

    public static object Overloaded(Func<object> function) => null;

    public static T Infer<T>(Expression<Func<string, T>> expression) => default(T);
}
