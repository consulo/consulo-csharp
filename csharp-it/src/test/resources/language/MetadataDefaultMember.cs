// sdk:NET_10
// an indexer of a library is a property with parameters - its type has DefaultMemberAttribute with its name
using Lib;

class Test
{
    void Method(/*attribute:!System.Reflection.DefaultMemberAttribute*/Table table, Plain plain)
    {
        var /*type:string*/cell = table[1, 2];
        var /*type:int*/item = plain[1];
    }
}
