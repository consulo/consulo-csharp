using Test;

public class NestedBug
{
	public static void test()
	{
		Impl<string> str;

		SomeInterface<string> test = str.Get();
	}
}