public class C
{
    public static void Main()
    {
        int _ = 42;
        System.Func<string, string, int> f = (_, _) =>
        {
            System.Func<string, string, int> f2 = (_, _) => ++/*@symbol:System.Int32 _*/_;
            return f2(null, null);
        };
        System.Console.Write(f(null, null));
    }
}