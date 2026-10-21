// expectedOutput: 2
class Test
{
    static void Fn(string x = "", string y = "", params object[] p) 
    { 
        System.Console.WriteLine(1); 
    }

    static void Fn(string x, params object[] p)
    { 
        System.Console.WriteLine(2); 
    }

    static void Main()
    { Fn("Hello", p: "World"); }
}
