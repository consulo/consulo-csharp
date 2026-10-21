// expectedOutput: Run1(Action action)\nRun1(Action action)\nRun2(Action action)\nRun2(Action action)
using System;

class C
{
    public static void Main()
    {
        Run1(() => MethodReturnsVoid());
        Run1(MethodReturnsVoid);
        Run2(() => MethodReturnsVoid());
        Run2(MethodReturnsVoid);
    }

    public static object Run1(Action action)
    {
        Console.WriteLine("Run1(Action action)");
        action();
        return null;
    }

    public static object Run1(Func<object> action, bool optional = false)
    {
        Console.WriteLine("Run1(Func<object> action, bool optional = false)");
        return action();
    }

    public static object Run2(Func<object> action, bool optional = false)
    {
        Console.WriteLine("Run2(Func<object> action, bool optional = false)");
        return action();
    }

    public static object Run2(Action action)
    {
        Console.WriteLine("Run2(Action action)");
        action();
        return null;
    }

    private static void MethodReturnsVoid()
    {
    }
}
