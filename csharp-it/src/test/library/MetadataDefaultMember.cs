using System.Runtime.CompilerServices;

namespace Lib
{
    // DefaultMemberAttribute - the name of the indexers of a type: Item, or the one of IndexerNameAttribute
    public class Table
    {
        [IndexerName("Cell")]
        public string this[int row, int column] => "";
    }

    public class Plain
    {
        public int this[int index] => index;
    }
}
