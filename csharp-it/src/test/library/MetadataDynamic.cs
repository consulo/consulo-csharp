using System.Collections.Generic;

namespace Lib
{
    // DynamicAttribute - [Dynamic] on the declaration, or [Dynamic(bool[])] with a flag for each type of its type tree
    public class Dynamics
    {
        public dynamic Field = null!;
        public object Object = null!;
        public dynamic[] Array = null!;
        public List<dynamic> List = null!;
        public Dictionary<string, dynamic[]> Map = null!;
        public dynamic Property { get; set; } = null!;
    }
}
