class C { bool M(int[] a) => a is [1, 2, .., var last] || a is [] || a is [_, .. var rest]; }
