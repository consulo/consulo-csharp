// expectedOutput: 2
class Program
{
    public static void Main()
    {
        Program p = new Program();
        p.Main2();
    }
    void Main2()
    {
        this.M(null);
    }
    public static void M(A a) { System.Console.WriteLine(1); }
    public void M(B b) { System.Console.WriteLine(2); }
}
class A {}
class B {}
