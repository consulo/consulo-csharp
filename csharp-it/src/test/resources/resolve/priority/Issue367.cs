namespace Issue367
{
	public class UnityEvent
	{
		public static object GetDelegate()
		{
			return default(object);
		}
	}

	public class UnityEvent<T0>
	{
		public void AddListener()
		{
			UnityEvent<T0>.GetDelegate(); // wrong generics count, due resolved to UnityEvent not UnityEvent<T0>
		}

		public static T0 GetDelegate()
		{
			return default(T0);
		}
	}
}