using System;
using System.Collections.Generic;

class C
{
    class DynamicWrapper
    {
        public IEnumerable<dynamic> Value { get; set; }
    }

    static void M()
    {
        DynamicWrapper[] array = null;
        Goo(array, x => x.Value, (x, y) => string.Empty); //-C.Goo(System.Collections.Generic.IEnumerable<C.DynamicWrapper>, System.Func<C.DynamicWrapper, System.Collections.Generic.IEnumerable<dynamic>>, System.Func<C.DynamicWrapper, dynamic, string>)
    }

    static IEnumerable<dynamic> Goo(
        object source,
        Func<dynamic, IEnumerable<dynamic>> collectionSelector,
        Func<dynamic, dynamic, dynamic> resultSelector)
    {
        return null;
    }

    static IEnumerable<string> Goo(
        IEnumerable<DynamicWrapper> source,
        Func<DynamicWrapper, IEnumerable<dynamic>> collectionSelector,
        Func<DynamicWrapper, dynamic, string> resultSelector)
    {
        return null;
    }
}