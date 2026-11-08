// sdk:NET_10
// what a lambda converted to an expression tree may not contain - a lambda inside it is a part of the tree too
using System;
using System.Linq.Expressions;
using System.Threading.Tasks;

class Test
{
    int F;

    static int Out(out int value)
    {
        value = 0;
        return 0;
    }

    static ref int Ref() => throw null;

    void Method(string s, object o, int[] items)
    {
        Expression<Func<int>> statement = /*error:CS0834*/() => { return 1; };
        Expression<Func<int>> assignment = () => /*error:CS0832*/F = 1;
        Expression<Func<int>> increment = () => /*error:CS0832*/F++;
        Expression<Func<int>> compound = () => /*error:CS0832*/F += 1;
        Expression<Func<int>> nullPropagating = () => /*error:CS8072*/s?.Length ?? 0;
        Expression<Func<Task>> async = /*error:CS1989*/async () => await Task.CompletedTask;
        Expression<Func<Func<int>>> anonymous = () => /*error:CS1945*/delegate { return 1; };
        Expression<Func<(int, int)>> tuple = () => /*error:CS8143*/(1, 2);
        int Local() => 1;
        Expression<Func<int>> local = () => /*error:CS8110*/Local();
        Expression<Func<int[,]>> array = () => /*error:CS0838*/new int[,] { { 1 } };
        Expression<Func<int>> switchExpression = () => /*error:CS8514*/o switch { int i => i, _ => 0 };
        Expression<Func<bool>> pattern = () => /*error:CS8122*/o is int value;
        Expression<Func<int>> throwExpression = () => s == null ? /*error:CS8188*/throw new Exception() : 1;
        Expression<Func<int>> outVariable = () => Out(/*error:CS8198*/out var x);
        Expression<Func<int>> discard = () => Out(/*error:CS8207*/out _);
        Expression<Func<int>> refReturn = () => /*error:CS8153*/Ref();
        Expression<Func<int[]>> collection = () => /*error:CS9175*/[1, 2];
        Expression<Func<int>> fromEnd = () => /*error:CS8790*/items[/*error:CS8791*/^1];
        Expression<Func<int[]>> range = () => /*error:CS8790*/items[/*error:CS8792*/1..2];
        Expression<Func<Func<int, int>>> nested = () => y => /*error:CS0832*/F = y;
        Take(() => /*error:CS0832*/F = 2);

        // allowed in an expression tree
        Expression<Func<int>> isType = () => o is int ? 1 : 0;
        Expression<Func<string>> coalesce = () => s ?? "";

        // a delegate is no expression tree
        Func<int> function = () => { F = 1; return s?.Length ?? 0; };
        Func<int> asDelegate = delegate { return 1; };
        Func<(int, int)> delegateTuple = () => (1, 2);
        Func<int[]> delegateRange = () => items[1..^1];
    }

    static void Take(Expression<Func<int>> expression)
    {
    }
}
