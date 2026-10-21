using System;
using System.Linq.Expressions;
 
public class SomeType
{
    public string SomeProperty { get; set; }
}
public class IntelliSenseError
{
    public static void Test1<T>(Expression<Func<T, object>> expr)
    {
        Console.WriteLine(((MemberExpression)expr.Body).Member.Name);
    }
    public static void Test2<T>(Expression<Func<T, object>> expr, bool additionalParameter)
    {
        Test1(expr);
    }
    public static void Main()
    {
        Test2<SomeType>(o => /*@type:SomeType*/o/* */.
    }
}
