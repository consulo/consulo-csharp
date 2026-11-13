class C { void M(int x) { var y = x >>> 2; x >>>= 1; } public static C operator >>>(C c, int s) => c; }
