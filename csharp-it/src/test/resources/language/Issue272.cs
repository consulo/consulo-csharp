using System;
using System.Reflection;

[assembly: AssemblyTitle("title")]
[assembly: AssemblyVersion("1.0"), AssemblyCulture("")]
[module: CLSCompliant(true)]
[Serializable]
public class First
{
}

[/*error:CS1730*/assembly: AssemblyDescription("late")]

namespace Inner
{
    [/*error:CS1730*/module: CLSCompliant(true)]
    [Serializable]
    public class Second
    {
    }

    /*error:CS0116*/[Obsolete]
}

/*error:CS0116*/[Obsolete]
