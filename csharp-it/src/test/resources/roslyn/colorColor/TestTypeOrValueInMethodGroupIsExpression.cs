class Color
{
    public void M() { }
    public static void M(int x) { }
}

class C
{
    void M()
    {
        Color Color = null;
        System.Console.WriteLine(/*@symbol:Local Color Color*/Color.M is object);
    }
}
