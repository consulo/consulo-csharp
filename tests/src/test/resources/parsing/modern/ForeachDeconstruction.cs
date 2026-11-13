class C { void M(System.Collections.Generic.Dictionary<string, int> d) { foreach (var (k, v) in d) { } foreach ((string k, int v) in d) { } } }
