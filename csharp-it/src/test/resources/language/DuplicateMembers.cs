class C
{
    void M(int a) { }
    void /*error:CS0111*/M(int b) { }
    void M(string s) { }
    int F;
    int /*error:CS0102*/F;
    int P { get; set; }
    void /*error:CS0102*/P() { }
    int this[int i] => i;
    int /*error:CS0111*/this[int j] => j;
    int this[string s] => 0;
    C(int x) { }
    /*error:CS0111*/C(int y) { }
    C() { }
    void G<T>(T t) { }
    void /*error:CS0111*/G<U>(U u) { }
    void G<T>(int t) { }
    public static int operator +(C a, C b) => 0;
    void R(ref int x) { }
    void R(in int x) { }
}
