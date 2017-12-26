public enum TargetType
{
	Test
}

public class AA
{
	public void test()
	{
	}

	public static void tt()
	{
	}
}

public class Program
{

	public static TargetType TargetType = TargetType.Test;  // error TargetType resolved to field, but need resolved to enum

	public static TargetType type = TargetType.Test;

	public static AA AA;

	public static void Main(string[] args)
	{
		int b = (int)TargetType.Test; // TargetType resolved to ENUM

		TargetType.GetHashCode();   // TargetType resolved to field


		AA.test();  // resolved to field

		AA.tt();    // resolved to class
	}
}