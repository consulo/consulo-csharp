using System.Collections.Generic;
 
class C
{
    static void M(IList<C> c)
    {
        var tmp = new C();
        tmp.M((a, b) => /*@symbol:System.Collections.Generic.ICollection.Add(C)*/c.Add);
    }

    static void M(System.Func<int, int, System.Action<C>> x)
    {}
}

