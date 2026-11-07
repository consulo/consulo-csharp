using System.Collections.Generic;

namespace Lib
{
    // ParamCollectionAttribute - params of a collection which is not an array (C# 13)
    public static class Collections
    {
        public static int List(params List<string> values) => values.Count;
        public static int Enumerable(params IEnumerable<long> values) => 0;
        public static int Array(params int[] values) => values.Length;
    }
}
