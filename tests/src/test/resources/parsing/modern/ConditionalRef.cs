class C { void M(bool c, int[] a, int[] b) { ref int r = ref (c ? ref a[0] : ref b[0]); } }
