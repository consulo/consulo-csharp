// sdk:NET_10
// ^1 is an index from the end, a..b a range (C# 8)
using System;
using System.Collections.Generic;

class Test
{
    void Method(int[] array, string text, List<int> list, int count)
    {
        var /*type:System.Index*/last = ^1;
        var /*type:System.Index*/fromEnd = ^count;
        var /*type:System.Range*/range = 1..3;
        var /*type:System.Range*/open = ..;
        var /*type:System.Range*/head = ..2;
        var /*type:System.Range*/tail = 1..;
        var /*type:System.Range*/ends = ^2..^1;
        var /*type:System.Range*/computed = (count - 1)..(count + 1);
        var /*type:int*/xor = count ^ 1;
        var /*type:double*/fraction = 1.5;

        var /*type:int*/lastItem = array[^1];
        var /*type:int[]*/slice = array[1..3];
        var /*type:char*/lastChar = text[^1];
        var /*type:string*/substring = text[1..];
        var /*type:int*/lastOfList = list[^1];
        Index index = ^1;
        Range whole = ..;
        var /*type:int*/byIndex = array[index];
        var /*type:System.Range*/literals = 1..2;
        var /*type:double*/afterRange = 1..2 is Range ? .5 : 1.0;
        Span<int> span = array;
        var /*type:System.Span<int>*/spanSlice = span[1..];
    }
}
