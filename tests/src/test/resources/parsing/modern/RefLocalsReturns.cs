class C { int[] a; ref int M() { ref int x = ref a[0]; ref readonly int y = ref a[1]; return ref a[0]; } }
