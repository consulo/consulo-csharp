// a lambda with a natural type (C# 10) converts to LambdaExpression and Expression too - as Expression<Func<int, int>>
using System;
using System.Linq.Expressions;

class Test
{
    void Method()
    {
        LambdaExpression lambda = (int x) => x;
        Expression expression = () => 1;
        var /*type:System.Type*/type = lambda.ReturnType;
        Take((string s) => s.Length);

        // an expression tree as Expression<Func<...>> is
        LambdaExpression statement = /*error:CS0834*/(int x) => { return x; };
        Expression assignment = (int x) => /*error:CS0832*/Field = x;
    }

    int Field;

    static void Take(LambdaExpression expression)
    {
    }
}
