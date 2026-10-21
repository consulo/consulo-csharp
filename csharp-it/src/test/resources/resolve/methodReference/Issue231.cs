using System;

public class Test
{
	public Action Test;
}

public class Program
{

	public static void Main()
	{
		Test t = new Test
		{
			Test = MyInvoker
		};
	}

	private void MyInvoker()
	{

	}
}