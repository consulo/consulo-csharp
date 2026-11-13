class C { C? n; int v; int[]? a; void M(C? c) { c?.v = 1; c?.n?.v += 2; a?[0] = 3; } }
