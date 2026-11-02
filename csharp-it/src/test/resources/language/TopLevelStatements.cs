using System;
using System.Threading.Tasks;

var /*type:string[]*/arguments = args;
int count = arguments.Length;
var /*type:string*/greeting = Greet("x");
Console.WriteLine(greeting + Helper.Twice(count));
await Task.Delay(1);

int Local(int value)
{
    return value + count;
}

if (Local(count) > 0)
{
    return 1;
}
return 0;

static string Greet(string name) => "Hi " + name;

public static class Helper
{
    public static int Twice(int value) => value * 2;
}
