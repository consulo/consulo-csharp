class B { }
interface I { }
class /*warning:CS0660*//*warning:CS0661*/C
{
    public C operator /*error:CS0558*/-(C a) => a;
    static C operator /*error:CS0558*/~(C a) => a;
    public static int operator /*error:CS0562*/!(int a) => 0;
    public static int operator /*error:CS0563*/*(int a, int b) => 0;
    public static C operator /*error:CS0564*/<<(int a, C b) => b;
    public static C operator >>(C a, string b) => a;
    public static int operator /*error:CS0559*/++(int a) => a;
    public static int operator /*error:CS0448*/--(C a) => 0;
    public static void operator /*error:CS0590*//(C a, C b) { }
    public static bool operator /*error:CS0216*/==(C a, C b) => true;
    public static bool operator /*error:CS0216*/<(C a, C b) => true;
    public static int operator /*error:CS0215*//*error:CS0216*/true(C a) => 0;
    public static bool operator /*error:CS0216*/false(C a) => false;
    public static implicit operator /*error:CS0552*/I(C a) => null;
    public static implicit operator B(C a) => null;
    public void operator +=(C a) { }
    public static explicit operator int(C a) => 0;
    public static explicit operator /*error:CS0557*/int(C a) => 1;
}
class E : B
{
    public static implicit operator /*error:CS0553*/B(E e) => null;
    public static implicit operator /*error:CS0553*/E(B e) => null;
}
struct S
{
    public static S operator -(S? s) => default;
    public static S operator +(S a, S b) => a;
    public static bool operator true(S s) => true;
    public static bool operator false(S s) => false;
}
