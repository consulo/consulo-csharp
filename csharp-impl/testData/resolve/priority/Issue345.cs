public class Program
{
	public static void Main(string[] args)
	{
		test("test", "test");
	}

	public static void test(string arg1, string arg2)
	{
		//Console.WriteLine("test0");
	}

	int test(params string[] args)
	{
		//Console.WriteLine("test1");
		return 1;
	}
}