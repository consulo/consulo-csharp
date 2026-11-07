// sdk:NET_10
// a ref struct of a library is obsolete for a compiler without the feature of its CompilerFeatureRequiredAttribute only
using Lib;

class Test
{
    void Method()
    {
        /*attribute:!System.ObsoleteAttribute*/Buffer buffer = default;
        Old./*attribute:System.ObsoleteAttribute*/Method();
    }
}
