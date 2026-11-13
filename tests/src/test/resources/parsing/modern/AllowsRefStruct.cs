class C<T> where T : allows ref struct {} class D<T> where T : struct, allows ref struct {} ref struct R : System.IDisposable { public void Dispose() {} }
