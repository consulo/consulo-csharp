using System;

class LambdaConversion
{
    static int Pick(Func<int> f) => 0;

    static string Pick(Action a) => "";

    void F() { }

    void M()
    {
        var /*type:int*/throws = Pick(() => { throw new Exception(); });
        var /*type:string*/statement = Pick(() => { F(); });
        var /*type:string*/empty = Pick(() => { });
        var /*type:int*/loop = Pick(() => { while (true) { } });
        var /*type:string*/returns = Pick(() => { if (true) return; });
        var /*type:int*/value = Pick(() => { return 1; });
        var /*type:int*/expression = Pick(() => 1);
    }
}
