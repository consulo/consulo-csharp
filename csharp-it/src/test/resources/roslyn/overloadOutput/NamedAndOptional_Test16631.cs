// expectedOutput: 2
    
public abstract class B
{
    protected abstract void E<T>();
}

public class D : B
{
    void M()
    {
        // There are two possible methods to choose here. The static method
        // is better because it is declared in a more derived class; the
        // virtual method is better because it has exactly the right number
        // of parameters. In this case, the static method wins. The virtual
        // method is to be treated as though it was a method of the base class,
        // and therefore automatically loses.  (The bug we are regressing here
        // is that Roslyn did not correctly identify the originally-defining
        // type B when the method E was generic. The original repro scenario in
        // bug 16631 was much more complicated than this, but it boiled down to
        // overload resolution choosing the wrong method.)

        E<int>();
    }

    protected override void E<T>()
    {
        System.Console.WriteLine(1);
    }

    static void E<T>(int x = 0xBEEF)
    {
        System.Console.WriteLine(2);
    }

    static void Main()
    {
        (new D()).M();
    }
}
