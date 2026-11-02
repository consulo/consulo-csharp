using System.Collections.Generic;

namespace Cs.Arrays
{
    public enum Kind
    {
        First,
        Second
    }

    public static class Initializers
    {
        public static void Run()
        {
            short[] fromEnum = new short[] { /*error:CS0029*/Kind.First, /*error:CS0029*/Kind.Second };
            int[] implicitArray = { 1, /*error:CS0029*/"two", 3 };
            string[] names = new string[] { "a", null, /*error:CS0029*/1 };
            short[] small = new short[] { 1, 2 };
            object[] objects = new object[] { 1, "a", null };
            int[][] jagged = new int[][] { new int[] { 1 }, new int[0] };
            int[,] matrix = new int[,] { { 1, 2 }, { 3, 4 } };
            var list = new List<int> { 1, 2 };
            var inferred = new[] { 1, 2 };
        }
    }
}
