using System;

public class Class1<T>
{
}

public class ClassWithGeneric<T, A>
{
    public class D
    {

    }
    public static void helloWorld(T t)
    {

    }
}
public class Program
{
    public static void Main(string[] args)
    {
        var t = typeof(ClassWithGeneric<,>.D);
        var t2 = typeof(ClassWithGeneric<Int32, String>);
        var t3= typeof(Class1<>);

        Console.WriteLine(t.FullName);
    }
}