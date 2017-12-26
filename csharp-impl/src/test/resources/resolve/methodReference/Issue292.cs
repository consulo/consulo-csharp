using System;

namespace Issue292
{
	public class SomeNotifier
	{

	}
}
namespace Issue292
{
	public class Program
	{
		public static void Main()
		{

		}

		private void test() {
			Action<String> someEvent = null;

			someEvent = SomeNotifier; // error cant cast Action<String> to Issue292.SomeNotifier
		}

		private void SomeNotifier(String param) {

		}
	}
}