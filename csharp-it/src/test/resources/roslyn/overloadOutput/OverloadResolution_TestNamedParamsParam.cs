// expectedOutput: 2
class C
{
    static void M(
        int x,
        double y,
        params string[] z)
    {
        System.Console.WriteLine(1);
    }
    
    static void M(
        int x,
        params string[] z)
    {
        System.Console.WriteLine(2);
    }
    static void Main()
    {
        C.M(0, z: "");
    }
}