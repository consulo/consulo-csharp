class C { string M(string s) { var x = s ?? throw new System.Exception(); return s is null ? throw new System.Exception() : s; } }
