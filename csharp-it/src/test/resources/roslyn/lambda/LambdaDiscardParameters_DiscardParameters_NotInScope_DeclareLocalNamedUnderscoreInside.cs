class C
{
    static void M()
    {
        System.Func<string, string, long> f = (_, _) => { long _ = 0; return /*@symbol:System.Int64 _*/_++; };
        System.Func<string, string, long> f2 = (_, a) => {
            long _ = 0; // 1
            return /*@symbol:System.Int64 _*/_++;
        };
    }
}