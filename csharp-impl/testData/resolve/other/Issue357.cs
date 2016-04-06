using System;

public class SomeAPI
{
	public string SomeString
	{
		get
		{
			Reflection.Assembly assembly = null; // error here
			return "";
		}
	}
}