class C { string M(int x) => $"a{x switch
{
    1 => "one",
    _ => "other"
}}b"; }
