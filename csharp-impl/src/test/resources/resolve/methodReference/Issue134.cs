public static class EE
{
	public static void CallInvoke(this System.Action a)
	{

	}
}

public class B
{
	private static System.Action action;
	private static System.Action<String> action2;


	public static void test(string[] args)
	{
		test(action.CallInvoke);
	}

	private static void test(System.Action a)
	{

	}
}