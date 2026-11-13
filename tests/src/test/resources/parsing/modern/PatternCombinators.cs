class C { bool M(object o, int i) => o is not null and not string && i is > 0 and < 10 or 42 && o is (int or long); }
