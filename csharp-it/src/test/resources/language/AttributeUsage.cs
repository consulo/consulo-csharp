using System;

[AttributeUsage(AttributeTargets.Method)]
class OnlyMethodAttribute : Attribute { }

[AttributeUsage(AttributeTargets.All, AllowMultiple = true)]
class MultiAttribute : Attribute { }

class SingleAttribute : Attribute { }

class DerivedAttribute : OnlyMethodAttribute { }

class NotAttr { }

[/*error:CS0592*/OnlyMethod]
class C
{
    [Single, /*error:CS0579*/Single]
    void M() { }

    [Multi, Multi]
    void N() { }

    [/*error:CS0616*/NotAttr]
    int F;

    [return: /*error:CS0592*/OnlyMethod]
    int R() => 0;

    [/*warning:CS0657*/method: Single]
    int P { get; set; }

    [field: Single]
    int Q { get; set; }

    [/*warning:CS0657*/type: Single]
    void T() { }

    [Obsolete] [/*error:CS0579*/Obsolete]
    void O() { }

    [Single] [return: Single]
    int U() => 0;

    [/*error:CS0592*/Derived]
    int V;

    [OnlyMethod, Derived]
    void W() { }
}

[Single]
[/*error:CS0579*/Single]
struct S { }
