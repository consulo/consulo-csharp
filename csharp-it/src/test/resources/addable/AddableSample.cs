// The source of AddableSample.dll - an assembly of the SDK of the test, not referenced by its module:
// dotnet exec <sdk>/Roslyn/bincore/csc.dll -nologo -target:library -nostdlib -noconfig -deterministic
//     -reference:<mockSdk/net10.0>/*.dll -out:AddableSample.dll AddableSample.cs
using System;

namespace Addable.Api
{
    public class Widget { }
    public static class WidgetExtensions
    {
        public static int Count(this Widget widget) => 0;
    }
    public sealed class MarkerAttribute : Attribute { }
}
