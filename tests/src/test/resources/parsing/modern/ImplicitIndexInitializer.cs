class C { int[] a = new int[3]; static C M() => new C { a = { [^1] = 2 } }; }
