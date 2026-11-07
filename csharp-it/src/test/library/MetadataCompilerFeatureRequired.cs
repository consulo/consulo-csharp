using System;

namespace Lib
{
    // CompilerFeatureRequiredAttribute - with ObsoleteAttribute, for a compiler without the feature
    public ref struct Buffer
    {
        public int Length;
    }

    public static class Old
    {
        [Obsolete("old")]
        public static void Method()
        {
        }
    }
}
