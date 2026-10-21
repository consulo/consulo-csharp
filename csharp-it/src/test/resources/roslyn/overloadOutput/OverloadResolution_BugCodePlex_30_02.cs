// expectedOutput: 1\n1
using System;
class C
{
    static void Main()
    {
        Test(false);
    }

    static void Test(bool val)
    {
        Goo(() => { if (val) return () => 0; else return () => (short)0; });
        Goo(() => { if (val) return () => (short)0; else return () => 0; });
    }
    static void Goo(Func<Func<short>> x) { Console.WriteLine(1); }
    static void Goo(Func<Func<int>> x) { Console.WriteLine(2); }
}
