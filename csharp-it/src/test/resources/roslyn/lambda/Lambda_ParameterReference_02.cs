using System;

class Program
{
    static void stuff()
    {
        Func<Program, string> l = a => /*@symbol:Program a*/a.
    }
}
