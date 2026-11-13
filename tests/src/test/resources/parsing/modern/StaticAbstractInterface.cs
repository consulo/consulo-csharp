interface I<T> where T : I<T> { static abstract T Zero { get; } static virtual int One() => 1; static abstract T operator +(T a, T b); }
