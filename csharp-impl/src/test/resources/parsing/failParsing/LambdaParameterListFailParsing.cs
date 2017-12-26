public class LambdaParameterListFailParsing
{
	public void test(SomeEnum e)
	{
		bebe((int i, ) => {});

		bebe((i, ) => {});

		bebe((ref int i, ) => {});

		bebe(i => {});
	}
}