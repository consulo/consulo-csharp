using System;

public class Program
{
    public static void Main(string[] myArguments)
    {
        String template = "";
        int value = 1;
        var o = new
        {
            String.Empty,
            value,
            AA = 1
        };

        Console.WriteLine(o.Empty);
        Console.WriteLine(o.AA);
    }
}