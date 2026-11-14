interface I { }
interface K { }
class A { }
class B { }
struct S1 : /*error:CS0527*/A { }
interface J : /*error:CS0527*/A { }
class C1 : A, /*error:CS1721*/B { }
class C2 : I, /*error:CS0528*/I { }
class C3 : A, I, K { }
struct S2 { protected int /*error:CS0666*/x; protected void /*error:CS0666*/M() { } public override string ToString() => ""; }
struct S3 { S4 /*error:CS0523*/f; }
struct S4 { S3 /*error:CS0523*/g; }
struct S5 { S5 /*error:CS0523*/self; }
struct S6 { static S6 ok; S6? /*error:CS0523*/n; }
struct S7 { S7 /*error:CS0523*/P { get; set; } int Q => 0; }
struct S8 { S1 a; int b; }
enum E1 { A = 1, B = A }
enum E2 : byte { A = /*error:CS0031*/300, B = /*error:CS0031*/-1, C = 255 }
enum E3 : ushort { A = 65535 }
class Constants
{
    byte b1 = /*error:CS0031*/300;
    byte b2 = 200;
    sbyte s1 = /*error:CS0031*/-129;
    ushort u = /*error:CS0031*/-1;
    E2 e = E2.C;
    void M(byte p) { M(1); }
}
