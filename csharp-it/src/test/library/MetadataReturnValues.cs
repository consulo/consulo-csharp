using System;

namespace Lib
{
    // the return value of a method: by reference, and with the attributes of its .param [0] - tuple element names, dynamic, ref readonly
    public class Returns
    {
        private int myValue;

        public static (int Id, string Name) Tuple() => default;

        public static dynamic Dynamic() => null!;

        public ref int Ref() => ref myValue;

        public ref readonly int RefReadonly() => ref myValue;

        public ref readonly int Property => ref myValue;

        public static int Span(params ReadOnlySpan<int> values) => values.Length;
    }
}
