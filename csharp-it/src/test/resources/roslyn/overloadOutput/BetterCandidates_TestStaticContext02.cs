// expectedOutput: 1
class Program
{
    public static void Main()
    {
        new Program();
    }
    public static int M(A a) { System.Console.WriteLine(1); return 1; }
    public int M(B b) { System.Console.WriteLine(2); return 2; }
    int X = M(null);
}
class A {}
class B {}
