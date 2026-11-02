using System;

[assembly: AssemblyTitle("a")]
[assembly: AssemblyVersion("1.0"), AssemblyCulture("")]
[Serializable]
public class First
{
}

namespace Inner
{
    [module: Unverifiable]
    [field: NonSerialized]
    public class Second
    {
        [assembly: Wrong]
        public int Value;
    }

    [Obsolete]
}

[Obsolete] public
