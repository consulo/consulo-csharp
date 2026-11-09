using System;

partial class A { }
class /*error:CS0260*/A { }

class /*error:CS0260*/K { }
partial class K { }

partial class B { }
partial struct /*error:CS0261*/B { }

partial interface II { }
partial class /*error:CS0261*/II { }

public partial class /*error:CS0262*/C { }
internal partial class C { }

partial class D { }
public partial class D { }

class Base1 { }
class Base2 { }

partial class /*error:CS0263*/E : Base1 { }
partial class E : Base2 { }

partial class F : Base1 { }
partial class F : IDisposable { public void Dispose() { } }

partial class /*error:CS0265*/G<T> where T : class { }
partial class G<T> where T : struct { }

partial class H<T> where T : class { }
partial class H<T> { }

partial class M<T, U> where T : IComparable<T> where U : new() { }
partial class M<T, U> where U : new() where T : IComparable<T> { }

partial class /*error:CS0265*//*error:CS0265*/N<T, U> where T : Base1 where U : class { }
partial class N<T, U> where T : Base2 where U : struct { }

static partial class S { }
partial class S { }

class Outer
{
    partial class Inner { }
    class /*error:CS0260*/Inner { }
    partial struct Nested { }
    partial class /*error:CS0261*/Nested { }
}
