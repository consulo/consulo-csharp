class Program
{
    static void Main(string[] args)
    {
        // completion should work even in a syntactically invalid lambda
        var handler = new MyDelegateType((s, e) => { /*@type:MyArgumentType*/e. });
    }
}

public delegate void MyDelegateType(
    object sender,
    MyArgumentType e
);

public class MyArgumentType
{
    public int SomePublicMember;
}