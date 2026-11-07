using System;
using System.Collections.Generic;
using System.Linq;

namespace Cs.Collections
{
    public static class Keys
    {
        public static readonly Dictionary<string, Type> Map = new Dictionary<string, Type>();

        public static int Run()
        {
            var /*type:System.Collections.Generic.Dictionary<string, System.Type>.KeyCollection*/keys = Map.Keys;
            var /*type:string[]*/array = Map.Keys.ToArray();
            var all = array.Concat(new[] { "x" }).ToArray();
            foreach (var /*type:string*/key in Map.Keys)
            {
            }
            return array.Length + all.Length;
        }
    }
}
