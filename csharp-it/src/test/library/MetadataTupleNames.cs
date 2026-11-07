using System.Collections.Generic;

namespace Lib
{
    // TupleElementNamesAttribute - the element names of all tuples of the type tree, in prefix order; null for an unnamed one
    public class Tuples
    {
        public (int Id, string Name) Field;
        public (int, string Name) Partial;
        public (int Id, (string First, string Last) Name) Nested;
        public (int Id, string Name)[] Array = null!;
        public List<(int Id, string Name)> List = null!;
        public (int Id, string Name) Property { get; set; }
        public (int, string) Unnamed;
    }
}
