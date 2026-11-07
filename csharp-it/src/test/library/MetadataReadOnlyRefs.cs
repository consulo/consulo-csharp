namespace Lib
{
    // IsReadOnlyAttribute - an in parameter is a byref one with it, RequiresLocationAttribute a ref readonly one
    public static class ReadOnlyRefs
    {
        public static int In(in int value) => value;
        public static int RefReadonly(ref readonly int value) => value;
        public static int Ref(ref int value) => value;
    }

    public readonly struct Point
    {
        public readonly int X;

        public Point(int x)
        {
            X = x;
        }
    }
}
