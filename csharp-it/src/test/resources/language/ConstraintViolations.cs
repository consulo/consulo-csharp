using System;
interface I { }
class Base { }
class Derived : Base, I { }
class NoCtor { NoCtor(int x) { } }
abstract class Abs { }
struct S { }
class G1<T> where T : new() { }
class G2<T> where T : Base { }
class G3<T> where T : class { }
class G4<T> where T : struct { }
class G5<T> where T : I { }
class G6<T> where T : IComparable<T> { }
class /*error:CS0452*/D : G3<int> { }
class Use<U, V, W> where V : Base, new() where W : struct
{
    G1<NoCtor> /*error:CS0310*/a;
    G1<Abs> /*error:CS0310*/b;
    G1<Derived> c;
    G1<S> c2;
    G1<U> /*error:CS0310*/c3;
    G1<V> c4;
    G1<W> c5;
    G2<string> /*error:CS0311*/d;
    G2<Derived> e;
    G2<V> e2;
    G3<int> /*error:CS0452*/f;
    G3<string> g;
    G3<V> g2;
    G4<string> /*error:CS0453*/h;
    G4<int?> /*error:CS0453*/i;
    G4<W> i2;
    G5<S> /*error:CS0315*/j;
    G5<Derived> k;
    G2<U> /*error:CS0314*/l;
    G3<U> /*error:CS0452*/m;
    G6<int> n;
    G6<Base> /*error:CS0311*/o;
    Nullable<string> /*error:CS0453*/z;
    Nullable<int> z2;
    System.Collections.Generic.List<int> z3;
    static void M<T>() where T : Base { }
    void N()
    {
        /*error:CS0311*/M<string>();
        M<Derived>();
        G3</*error:CS0452*/int> local = null;
        var x = new G3</*error:CS0452*/int>();
        var t = typeof(G4</*error:CS0453*/string>);
        Console.WriteLine(local + "" + x + t);
    }
    void P(G1<Derived> x, G2<Derived> y, G3<string> z) { }
}
