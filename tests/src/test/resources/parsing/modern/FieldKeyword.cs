class C { public string P { get => field; set => field = value ?? ""; } public int Q { get; set { field = value; } } }
