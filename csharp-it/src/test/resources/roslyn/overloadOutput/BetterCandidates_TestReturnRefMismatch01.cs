// expectedOutput: 2
public class Program
{
    static int tmp;
    static void Main()
    {
        M(Q);
    }

    static void M(D1 d) { System.Console.WriteLine(1); }
    static void M(D2 d) { System.Console.WriteLine(2); }

    static ref int Q() { return ref tmp; }
}
delegate int D1();
delegate ref int D2();
