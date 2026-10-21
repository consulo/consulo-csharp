using System;

class Program
{
    static Func<Program, string> stuff()
    {
        return a => /*@symbol:Program a*/a.
    }
}
