namespace Cs.Refs
{
    public static class RefLocals
    {
        private static int[] ourValues = new int[4];

        public static ref int First() => ref ourValues[0];

        public static int Run()
        {
            ref int /*type:int*/first = ref ourValues[0];
            first = 10;
            ref readonly int /*type:int*/second = ref ourValues[1];
            ref var /*type:int*/third = ref First();
            third++;
            return first + second + third;
        }
    }
}
