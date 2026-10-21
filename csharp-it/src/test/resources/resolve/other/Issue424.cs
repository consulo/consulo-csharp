public struct SomeValue
{
	public static SomeValue operator-(SomeValue someValue, SomeValue someValue2)
	{
		return new SomeValue();
	}
}


public class Program
{
	public static void Main()
	{
		SomeValue? nullable = new SomeValue();

		SomeValue someValue = new SomeValue();

		nullable -= someValue;
	}
}