// sdk:NET_10
// params of a collection in a library is ParamCollectionAttribute, of an array ParamArrayAttribute
using Lib;

class Test
{
    void Method()
    {
        var /*type:int*/list = Collections.List("a", "b");
        var /*type:int*/none = Collections.List();
        var /*type:int*/enumerable = Collections.Enumerable(1, 2);
        var /*type:int*/array = Collections.Array(1, 2);
    }
}
