// expectedOutput: M1(int x)
class CTest
{
    static void Main()
    {
        M1(0);
    }

    static void M1(int x)
    {
        System.Console.WriteLine("M1(int x)");
    }

    static void M1(int x, int y = 0)
    {
        System.Console.WriteLine("M1(int x, int y = 0)");
    }

    static void M1(int x, params int [] y)
    {
        System.Console.WriteLine("M1(int x, params int [] y)");
    }
}
