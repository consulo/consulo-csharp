class C { unsafe void M() { int* p = stackalloc int[] { 1, 2 }; System.Span<int> s = stackalloc[] { 1, 2, 3 }; } }
