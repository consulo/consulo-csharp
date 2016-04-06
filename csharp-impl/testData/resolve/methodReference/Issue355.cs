using System;

public class Program
{
	public static void Main()
	{
		test(Test<String>);
	}

	public static void Test(Object param)
	{
		//Console.WriteLine("This should never called");
	}

	public static void Test<T>(T param)
	{
		//Console.WriteLine("Hello Word");
	}

	public static void test(Action<String> str)
	{
		//str.Invoke("test");
	}
}