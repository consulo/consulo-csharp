// sdk:NET_10
// dynamic in a library is object with DynamicAttribute
using Lib;

class Test
{
    void Method(Dynamics dynamics)
    {
        var /*type:dynamic*/field = dynamics.Field;
        var /*type:object*/value = dynamics.Object;
        var /*type:dynamic[]*/array = dynamics.Array;
        var /*type:System.Collections.Generic.List<dynamic>*/list = dynamics.List;
        var /*type:System.Collections.Generic.Dictionary<string, dynamic[]>*/map = dynamics.Map;
        var /*type:dynamic*/property = dynamics.Property;
    }
}
