class C { string M(int i) => i switch { < 0 => "n", >= 0 and <= 9 => "d", _ => "x" }; }
