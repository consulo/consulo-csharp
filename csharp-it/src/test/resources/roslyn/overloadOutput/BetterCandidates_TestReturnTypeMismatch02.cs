// expectedOutput: 2
public class Program
{
    static void Main()
    {
        M(new Z().Q);
    }

    static void M(D1 d) { System.Console.WriteLine(1); }
    static void M(D2 d) { System.Console.WriteLine(2); }
}
delegate int D1(A a);
delegate void D2(B b);

public class A {}
public class B {}
public class Z {}
public static class X
{
    public static void Q(this Z z, A a) {}
    public static void Q(this Z z, B b) {}
}
namespace System.Runtime.CompilerServices
{
    public class ExtensionAttribute : System.Attribute {}
}
