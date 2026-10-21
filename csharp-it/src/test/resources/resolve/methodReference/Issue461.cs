public class Test
{
	public delegate void MyDelegateType();

	public Test()
	{
		MyFunction<MyDelegateType>(() => this.MethodThatMatchDelegateSignature);
	}

	public void MethodThatMatchDelegateSignature()
	{
	}

	public void MyFunction<TMyDelegateType>(System.Func<TMyDelegateType> method)
	{
	}
}