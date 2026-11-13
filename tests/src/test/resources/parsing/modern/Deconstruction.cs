class C { void M() { var (a, b) = (1, 2); (int c, var d) = (1, 2); (a, b) = (b, a); } }
