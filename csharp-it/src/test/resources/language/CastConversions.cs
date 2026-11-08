// sdk:NET_10
// CS0030 is verified with csc (.NET 10 SDK): the casts without a marker compile
using System;
using System.Collections;
using System.Collections.Generic;

interface IShape { }
interface IOther { }
interface IGen<T> { }
interface ICo<out T> { }
interface IContra<in T> { }
class Base { }
class Derived : Base, IShape { }
sealed class Sealed { }
sealed class SealedShape : IShape { }
struct Point : IShape { }
struct Plain { }
enum Color { Red }
enum Size : byte { Small }
delegate void Handler(object o);
delegate void Handler2(object o);

class Meters
{
    public static explicit operator double(Meters m) => 0;
    public static implicit operator Meters(int i) => new Meters();
}

struct Celsius
{
    public static explicit operator Celsius(float f) => default;
}

class Generic<T> where T : Base
{
    void TypeParameters(T t, object o, IOther i, Base b)
    {
        var a1 = (Base) t;
        var a2 = /*error:CS0030*/(Derived) t;
        var a3 = (IOther) t;
        var a4 = (T) o;
        var a5 = (T) i;
        var a6 = (T) b;
        var a7 = /*error:CS0030*/(string) t;
    }
}

class GenericUnconstrained<U>
{
    void Unconstrained(U u, int n)
    {
        var b1 = (object) u;
        var b2 = (IShape) u;
        var b3 = (U) (object) n;
        var b4 = /*error:CS0030*/(int) u;
        var b5 = /*error:CS0030*/(U) n;
    }
}

class Program
{
    void Numeric(int i, long l, double d, decimal m, char c, float f)
    {
        var n1 = (byte) i;
        var n2 = (int) l;
        var n3 = (int) d;
        var n4 = (decimal) d;
        var n5 = (double) m;
        var n6 = (char) i;
        var n7 = (int) c;
        var n8 = (short) f;
        var n9 = (nint) l;
        var n10 = (long) i;
        var n11 = /*error:CS0030*/(bool) i;
        var n12 = /*error:CS0030*/(int) true;
        var n13 = /*error:CS0030*/(char) true;
    }

    void Enums(int i, Color color, Size size, double d)
    {
        var e1 = (Color) i;
        var e2 = (int) color;
        var e3 = (Color) size;
        var e4 = (Color) d;
        var e5 = (Size) 0;
        var e6 = /*error:CS0030*/(string) color;
        var e7 = /*error:CS0030*/(Color) "red";
    }

    void Nullables(int? ni, int i, double? nd, Point? np)
    {
        var u1 = (int) ni;
        var u2 = (int?) i;
        var u3 = (long?) ni;
        var u4 = (int?) nd;
        var u5 = (int) nd;
        var u6 = (Point) np;
        var u7 = /*error:CS0030*/(Plain?) np;
        var u8 = /*error:CS0030*/(string) ni;
    }

    void References(object o, Base b, Derived dv, IShape s, IOther oth, Sealed se, string str, Array arr, IEnumerable en)
    {
        var r1 = (Derived) b;
        var r2 = (Base) dv;
        var r3 = (object) str;
        var r4 = (string) o;
        var r5 = (IShape) b;
        var r6 = (Base) s;
        var r7 = (IOther) s;
        var r8 = /*error:CS0030*/(IShape) se;
        var r9 = /*error:CS0030*/(Sealed) s;
        var r10 = (SealedShape) s;
        var r11 = /*error:CS0030*/(Derived) str;
        var r12 = /*error:CS0030*/(string) b;
        var r13 = (int[]) arr;
        var r14 = (Array) new int[0];
        var r15 = (string) en;       // string implements IEnumerable
        var r16 = (Sealed) o;
    }

    void Arrays(object[] oa, string[] sa, Base[] ba, int[] ia, long[] la)
    {
        var a1 = (string[]) oa;
        var a2 = (object[]) sa;
        var a3 = (Derived[]) ba;
        var a4 = (IList<string>) sa;
        var a5 = (string[]) (IList<string>) sa;
        var a6 = (IEnumerable<object>) sa;
        var a7 = /*error:CS0030*/(long[]) ia;
        var a8 = /*error:CS0030*/(object[]) ia;
        var a9 = /*error:CS0030*/(int[,]) ia;
        var a10 = (IList<int>) ia;
    }

    void Boxing(object o, ValueType vt, IShape s, Point p, int i, IComparable c, Enum en, Color color)
    {
        var x1 = (object) p;
        var x2 = (Point) o;
        var x3 = (Point) vt;
        var x4 = (Point) s;          // interface implemented by the struct
        var x5 = /*error:CS0030*/(Plain) s;
        var x6 = (int) c;
        var x7 = (IShape) p;
        var x8 = /*error:CS0030*/(IOther) p;
        var x9 = (Color) en;
        var x10 = (Enum) color;
        var x11 = (int) o;
    }

    void Variance(ICo<object> co, ICo<string> cs, IContra<object> ko, IGen<object> go, IGen<string> gs, IEnumerable<object> eo)
    {
        var v1 = (ICo<object>) cs;
        var v2 = (ICo<string>) co;
        var v3 = (IContra<string>) ko;
        var v4 = (IGen<string>) go;
        var v5 = (List<string>) eo;
        var v6 = (IEnumerable<int>) eo;
    }

    void Delegates(Handler h, Delegate del, Action a, Func<int> fi, MulticastDelegate md)
    {
        var d1 = (Delegate) h;
        var d2 = (Handler) del;
        var d3 = (Handler) md;
        var d4 = /*error:CS0030*/(Handler2) h;
        var d5 = /*error:CS0030*/(Action) fi;
        var d6 = (Action<string>) (Action<object>) (x => { });
    }

    void UserDefined(Meters m, double d)
    {
        var w1 = (double) m;
        var w2 = (Meters) 5;
        var w3 = (Celsius) 2.5f;
        var w4 = (Celsius) 2;
        var w5 = (Meters) d;
        var w6 = (int) m;
    }

    void Misc(dynamic dyn, object o)
    {
        var m2 = (string) null;
        var m3 = (int?) null;
        var m4 = (Action) (() => { });
        var m5 = (Func<int, int>) (x => x);
        var m6 = (Action) Misc2;
        var m8 = ((int, string)) (1, "a");
        var m9 = ((long, object)) (1, "a");
        var m10 = (dynamic) o;
        var m11 = (IntPtr) 5;
        var m12 = (Span<int>) new int[1];
    }

    void Misc2() { }
}

