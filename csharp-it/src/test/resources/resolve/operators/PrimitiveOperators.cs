upublic class Test
{
	public enum Color
	{
		Red,
		Green
	}

	public void test()
	{
		int i = 1;
		long l = 2;
		byte b = 3;
		short s = 4;
		char c = 'a';
		float f = 1;
		double d = 2;
		decimal m = 3;
		uint u = 4;
		ulong ul = 5;
		string str = "";
		bool bo = true;
		int? ni = null;
		Color col = Color.Red;

		var r1 = i + i;
		var r2 = i + l;
		var r3 = b + b;
		var r4 = s * b;
		var r5 = c + i;
		var r6 = i + f;
		var r7 = l + d;
		var r8 = i + m;
		var r9 = i + u;
		var r10 = u + ul;
		var r11 = i == l;
		var r12 = i == 2;
		var r13 = b == c;
		var r14 = str + i;
		var r15 = i << 2;
		var r16 = l >> i;
		var r17 = i & b;
		var r18 = bo & bo;
		var r19 = ni + i;
		var r20 = col == Color.Green;
		var r21 = -b;
		var r22 = ~u;
		var r23 = i % 3;
		var r24 = d / i;
		var r25 = i < d;
		var r26 = !bo;
		i += b;
		l -= i;
	}
}
