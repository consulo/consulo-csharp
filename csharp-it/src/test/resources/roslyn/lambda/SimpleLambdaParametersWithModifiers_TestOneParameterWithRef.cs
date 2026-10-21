delegate void D(ref int x);

class C
{
    void M()
    {
        D d = /*@lambda:(ref System.Int32)*/(ref x) => { };
    }
}