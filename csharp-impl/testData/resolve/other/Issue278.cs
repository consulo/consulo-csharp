public sealed class FB
{
	private static string appId;

	public static void Init(int appId)
	{
		FB.appId = appId;
	}

	public abstract class RemoteFacebookLoader
	{
		public delegate void LoadedDllCallback();
	}
}