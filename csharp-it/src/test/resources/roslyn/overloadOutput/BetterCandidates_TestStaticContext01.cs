// expectedOutput: 1
class Program
{
    public static void Main()
    {
        M(null);
    }
    public static void M(A a) { System.Console.WriteLine(1); }
    public void M(B b) { System.Console.WriteLine(2); }
}
class A {}
class B {}
