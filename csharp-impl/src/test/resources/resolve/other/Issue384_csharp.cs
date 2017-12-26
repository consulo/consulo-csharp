public class ObjectA<T>
{
	public interface Collection : IEnumerable<T>
	{
	}

	public Collection Test
	{
		get;
		set;
	}
}

public class Program
{
	public static void Main(string[] args)
	{
		ObjectA<string> str = new ObjectA<string>();


		IEnumerable<string> test = str.Test;
	}
}