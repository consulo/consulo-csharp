public class Program
{
	static void TestInfiniteParams(params object[] items)
	{
	}

	public static void Main()
	{
		TestInfiniteParams(null, null);
		TestInfiniteParams("1st param", null, "3rd param", null);
		TestInfiniteParams("1st param", "2nd param", null);
	}
}