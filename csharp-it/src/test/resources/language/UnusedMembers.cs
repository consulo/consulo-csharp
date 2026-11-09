// check:unused
using System;
using System.Diagnostics;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Runtime.Serialization;

class C
{
    private void /*unused:CC0008*/Unused() { }
    private void Used() { }
    void /*unused:CC0008*/DefaultPrivate() { }
    private void Overload(int a) { }
    private void /*unused:CC0008*/Overload(string a) { }
    private int /*unused:CC0008*/UnusedProp { get; set; }
    private int UsedProp => 1;
    private event Action /*unused:CC0008*/UnusedEvent { add { } remove { } }
    private event Action UsedEvent { add { } remove { } }
    private static void Main() { }
    [Obsolete] private void /*unused:CC0008*/WithAttribute() { }
    [DllImport("x")] private static extern void Extern();
    private void ByNameof() { }
    private void ByGroup() { }
    private C() { }
    private /*unused:CC0008*/C(int x) { }
    private C(string s) { }
    private int /*unused:CC0008*/this[int i] => i;
    private int this[string s] => 0;
    private void Recursive() { Recursive(); }
    private void First() { Second(); }
    private void Second() { First(); }
    private static void Extension() { }
    public void Public() { }
    protected void Protected() { }
    internal void Internal() { }

    public void Run()
    {
        Used();
        Overload(1);
        int p = UsedProp + this["a"];
        UsedEvent += null;
        Console.WriteLine(nameof(ByNameof) + p);
        Action a = ByGroup;
        a();
        new C("s");
        C.Extension();
    }
}

interface I
{
    void Method();
}

class Impl : I
{
    void I.Method() { }
}

partial class P
{
    private void InOtherPart() { }
    private void /*unused:CC0008*/Unused() { }
}

partial class P
{
    void /*unused:CC0008*/M() { InOtherPart(); }
}

[StructLayout(LayoutKind.Sequential)]
class Layout
{
    private void Method() { }
}

[DebuggerDisplay("{Shown}")]
class Display
{
    private int Shown => 1;
    private int /*unused:CC0008*/Hidden => 2;
}

class Runtime
{
    [OnDeserialized] private void Deserialized(StreamingContext context) { }
    private void Handler(object sender, EventArgs e) { }
    private void /*unused:CC0008*/NotHandler(object sender, int e) { }
    private int Size { get; set; }
    private bool ShouldSerializeSize() => true;
    private void ResetSize() { }
    private void /*unused:CC0008*/ResetOther() { }
    private /*unused:CC0008*/Runtime(SerializationInfo info, StreamingContext context) { }
    public Runtime() { Size = 1; }
}

class Awaiter : INotifyCompletion
{
    public void OnCompleted(Action continuation) { }
    private bool IsCompleted => true;
    private void GetResult() { }
    private void /*unused:CC0008*/Other() { }
}

[Serializable]
class Serialized : ISerializable
{
    private Serialized(SerializationInfo info, StreamingContext context) { }
    public void GetObjectData(SerializationInfo info, StreamingContext context) { }
}
