using System;
using System.Collections.Generic;

public class Program
{
	public static void Main(string[] args)
	{
		List<String> listString = new List<string>();
		listString.Add("test");
		listString.Add("test2");
		listString.Add("test3");
		listString.Add("test4");

		using (List<string>.Enumerator enumerator = listString.GetEnumerator())
		{
			while(enumerator.MoveNext())
			{
				String value = enumerator.Current;

				Console.WriteLine(value);
			}
		}
	}
}