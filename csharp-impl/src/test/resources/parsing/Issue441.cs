namespace AA
{
	public static class Test
	{
		public static void Main()
		{
			int b = 1;

			switch(b)
			{
				case 1:
					break;
				case 2:
					if(true)
					{
						goto case 1;
					}
					break;
			}

			goto default;
		}
	}
}