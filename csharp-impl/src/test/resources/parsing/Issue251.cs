using System;
using System.Threading.Tasks;

public class Programm {

    Programm() {
        Object str = null;

        var s = str as String[] ?? new String[0];
        var s2 = str as String ?? "";
    }

    public static void Main()
    {
        Console.WriteLine("hello world");
    }
}