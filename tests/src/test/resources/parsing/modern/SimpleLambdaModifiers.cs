delegate bool D(ref int x, out int y); class C { void M() { D d = (ref x, out y) => { y = x; return true; }; System.Func<int, int> f = (scoped x) => x; } }
