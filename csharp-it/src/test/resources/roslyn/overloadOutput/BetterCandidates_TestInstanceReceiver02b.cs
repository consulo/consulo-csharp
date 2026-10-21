// expectedOutput: 2
class Program
{
    public static void Main()
    {
        D d = new D();
        d.Main2();
    }
    public static void M(A a) { System.Console.WriteLine(1); }
    public void M(B b) { System.Console.WriteLine(2); }
}
class D : Program
{
    public void Main2()
    {
        base.M(null);
    }
}
class A {}
class B {}
