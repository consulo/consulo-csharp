// sdk:NET_10
// the element names of a tuple in a library are in TupleElementNamesAttribute
using Lib;

class Test
{
    void Method(Tuples tuples)
    {
        var /*type:(int Id, string Name)*/field = tuples.Field;
        var /*type:int*/id = tuples.Field.Id;
        var /*type:string*/name = tuples.Property.Name;
        var /*type:(int Item1, string Name)*/partial = tuples.Partial;
        var /*type:string*/last = tuples.Nested.Name.Last;
        var /*type:(int Id, string Name)[]*/array = tuples.Array;
        var /*type:int*/arrayId = tuples.Array[0].Id;
        var /*type:System.Collections.Generic.List<(int Id, string Name)>*/list = tuples.List;
        var /*type:string*/listName = tuples.List[0].Name;
        var /*type:System.ValueTuple<int, string>*/unnamed = tuples.Unnamed;
    }
}
