
using System;

public class B<T>
{
    public void F(T p1) {  }
    public void F(dynamic p1) {  }
}

class C
{
    void M(B<C> b)
    {
        b.F(null); //-B<C>.F(C)
    }
}