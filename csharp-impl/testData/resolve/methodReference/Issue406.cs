public class Issue46
{
	public delegate bool Function<T>(T value);

	bool TestDelegate(int aValue)
	{
		return true;
	}

	public void Test<T>(Function<T> func, int b)
	{
	}

	private void Abc()
	{
		Test<int>(TestDelegate, 10);
	}
}