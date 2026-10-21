// expectedOutput: 2
public class Program
{
    static void Main()
    {
        M(new A(), 0);
    }

    static void M<T>(T t1, int i) where T: B { System.Console.WriteLine(1); }
    static void M(C c, short s) { System.Console.WriteLine(2); }
}
public class A {}
public class B {}
public class C { public static implicit operator C(A a) => null; }
