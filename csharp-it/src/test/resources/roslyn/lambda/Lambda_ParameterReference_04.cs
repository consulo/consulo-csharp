using System;

class Program
{
    static void stuff()
    {
        var l = (Func<Program, string>) (a => /*@symbol:Program a*/a.);
    }
}
