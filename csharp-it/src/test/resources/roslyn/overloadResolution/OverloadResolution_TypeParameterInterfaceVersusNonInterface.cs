
interface IA
{
    int Goo(int x = 0);
}
class C : IA
{
    public int Goo(int x)
    {
        return x;
    }
    static int M<T>(T x) where T : A, IA
    {
        return x.Goo(); //-IA.Goo(int)
    }
}
