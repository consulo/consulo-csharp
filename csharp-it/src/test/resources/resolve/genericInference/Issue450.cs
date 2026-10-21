public class AA
{
	public delegate TOutput SomeFunc<in TInput, out TOutput>(TInput input);

	public static void testMe()
	{
		GetInput(AA.Parse);
	}

	static T GetInput<T>(SomeFunc<string, T> transform)
	{
		return default(T);
	}

	public static int Parse(string s)
	{
	}
}