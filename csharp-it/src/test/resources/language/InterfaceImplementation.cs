using System;
interface I { void M(); int P { get; } }
interface J<T> { T Get(); }
interface K { void D() { } static abstract void S(); int Q => 0; int this[string s] => 0; static void T() { } }
abstract class A { public abstract void X(); }
class C1 : /*error:CS0535*//*error:CS0535*/I { }
class C2 : /*error:CS0738*/I { public int M() => 0; public int P => 0; }
class C3 : /*error:CS0736*/I { static public void M() { } public int P => 0; }
class C4 : /*error:CS0737*/I { void M() { } public int P => 0; }
class C5 : J<string> { public string Get() => ""; }
class C6 : /*error:CS0535*/J<int> { }
class C7 : K { public static void S() { } }
class /*error:CS0534*/C8 : A, I { void I.M() { } int I.P => 0; }
class C9 : /*error:CS0535*/IDisposable { }
class B10 { public void M() { } public int P => 0; }
class C10 : B10, I { }
interface L { int /*error:CS0525*/F; /*error:CS0526*/L() { } static int G; const int H = 1; }
class C11 { void /*error:CS0540*/I.M() { } }
interface I2 : I { void N(); }
class C12 : /*error:CS0535*//*error:CS0535*//*error:CS0535*/I2 { }
class B13 : I { public void M() { } public int P => 0; }
class C13 : B13, I2 { public void N() { } }
interface E { event Action Ev; int this[int i] { get; } }
class C14 : /*error:CS0535*//*error:CS0535*/E { }
abstract class C15 : I { public abstract void M(); public abstract int P { get; } }
partial class C16 : I { }
partial class C16 { public void M() { } public int P => 0; }
class C17 : I { public virtual void M() { } public int P => 0; }
class C18 : C17 { }
class C19 : C17, I { void I.M() { } }
