using System;

class Program
{
    static void stuff()
    {
         M1(a => /*@symbol:Program a*/a.);
    }

    static void M1(Func<Program, string> l){}
}
