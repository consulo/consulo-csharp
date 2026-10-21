// expectedOutput: void M1(string s, object o1, object o2)
public class Test
{
    static void M1(object o, object o1, string s, object o2 = null) 
    { 
        System.Console.WriteLine("void M1(object o, object o1, string s, object o2 = null) ");
    }

    static void M1(string s, object o1, object o2)
    {
        System.Console.WriteLine("void M1(string s, object o1, object o2)");
    }

    public static void Main()
    {
        M1("M", null, null);
    }
}
