namespace Issue475
{
	public delegate int TestMe(out int b);

	public class A
	{
		public void test(TestMe testMe)
		{
			test((out int b) =>
			{
				b = 1;
				return b;
			});

			int var = 1;

			testMe(out var);
		}
	}
}