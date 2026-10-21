public enum Lifetime
{
    Persistent,
    Transient,
    Scoped
}
public class Example
{
    public Lifetime Lifetime => /*@symbol:Type Lifetime*/Lifetime.Persistent;
    //                          ^^^^^^^^
}