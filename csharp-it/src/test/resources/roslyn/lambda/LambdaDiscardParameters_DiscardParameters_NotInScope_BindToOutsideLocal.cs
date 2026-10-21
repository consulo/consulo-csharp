public class C
{
    public static void Main()
    {
        int _ = 42;
        System.Func<string, string, int> f = (_, _) => ++/*@symbol:System.Int32 _*/_;
        System.Func<long, string, long> f2 = (_, a) => ++/*@symbol:System.Int64 _*/_;
        System.Console.Write(f(null, null) + " ");
        System.Console.Write(f2(1, null) + " ");
        System.Console.Write(_);
    }
}