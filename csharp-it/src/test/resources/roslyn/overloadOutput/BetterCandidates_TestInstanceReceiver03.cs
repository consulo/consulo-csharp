// expectedOutput: 2
class Program
{
    public static void Main()
    {
        new MyCollection { null };
    }
}
class A {}
class B {}
class MyCollection : System.Collections.IEnumerable
{
    public static void Add(A a) { System.Console.WriteLine(1); }
    public void Add(B b) { System.Console.WriteLine(2); }
    System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
    {
        throw new System.NotImplementedException();
    }
}
