// expectedOutput: struct class 
public class Program
{
    static void Main()
    {
        M(new A(), null);
        M(new B(), null);
    }

    static void M<T>(T t1, B b) where T: struct { System.Console.Write("struct "); }
    static void M<T>(T t1, X s) where T : class { System.Console.Write("class "); }
}
public struct A {}
public class B {}
public class X {}
