// expectedOutput: 2
public class Program
{
    static void Main()
    {
        M(Program.Q);
    }

    static void M(D1 d) { System.Console.WriteLine(1); }
    static void M(D2 d) { System.Console.WriteLine(2); }

    static void Q(A a) { }
    static void Q(B b) { }
}
delegate int D1(A a);
delegate void D2(B b);

class A {}
class B {}
