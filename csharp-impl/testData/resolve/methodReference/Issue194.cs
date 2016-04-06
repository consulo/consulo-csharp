public class CastChecks
{
	public delegate void Test<T>(T param);

	public static void Main()
	{
		Test<string> predicateString = new Test<string>(test);

		predicateString("te");
	}

	public static void test(string value)
	{

	}
}