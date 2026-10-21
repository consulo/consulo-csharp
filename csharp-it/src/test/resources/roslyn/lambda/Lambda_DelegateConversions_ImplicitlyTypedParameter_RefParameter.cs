struct R { }

delegate R D1(ref R r);

class Program
{
    static void Main()
    {
        D1 d1 = r1 => r1; // 1
        M(r2 => /*@symbol:R r2*/r2); // 2
    }

    static void M(D1 d1) { }
}