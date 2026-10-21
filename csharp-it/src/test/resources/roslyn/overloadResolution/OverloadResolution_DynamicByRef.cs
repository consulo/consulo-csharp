
using System;

public class C
{
	public static int F(int p1, char p2, ref dynamic p3) { return 2; }
	public static int F(C p1, params dynamic[] p2) { return 3; }

	public static implicit operator int(C t) { return 1; }
	public static implicit operator C(int t) { return new C(); }

	static void M()
	{            
		dynamic d1 = null;
		C c = null;
 
		C.F(c, 'a', ref d1); //-C.F(int, char, ref dynamic)
	}
}