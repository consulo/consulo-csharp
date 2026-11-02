// sdk:MONO_4_5
using System.Security.AccessControl;
using System.Security.Principal;

namespace Cs.Access
{
    public class Base
    {
        protected internal int both;
        private protected int derivedOfAssembly;
    }

    public class Derived : Base
    {
        public int Sum()
        {
            return both + derivedOfAssembly;
        }
    }

    public class Unrelated
    {
        public int Read(Base value)
        {
            return value.both;
        }
    }

    // protected internal of another assembly (famorassem) - visible to a derived type
    public class MyRule : AuthorizationRule
    {
        public MyRule(IdentityReference identity) : base(identity, 1, false, InheritanceFlags.None, PropagationFlags.None)
        {
        }

        public int Mask()
        {
            return AccessMask;
        }
    }
}
