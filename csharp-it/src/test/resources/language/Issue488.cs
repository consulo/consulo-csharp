namespace Cs.Enums
{
    public enum Plain
    {
        None,
        One = 1,
        Text = /*error:CS0029*/"text",
        Fraction = /*error:CS0029*/1.5
    }

    public enum Small : byte
    {
        A = 1,
        B = 200,
        Mixed = A | B,
        Shifted = 1 << 3
    }

    [System.Flags]
    public enum Flags
    {
        Read = 1,
        Write = 2,
        All = Read | Write,
        Next = All + 1
    }
}
