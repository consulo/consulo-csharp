delegate void D(ref int i);

class C
{
    void M()
    {
        D d = /*@lambda:(ref System.Int32)*/(ref a) => { };
    }
}