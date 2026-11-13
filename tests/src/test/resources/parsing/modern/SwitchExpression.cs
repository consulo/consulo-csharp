class C { int M(object o) => o switch { int i when i > 0 => 1, string => 2, null => 0, _ => -1 }; }
