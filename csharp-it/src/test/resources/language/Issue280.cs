using System.Collections.Generic;

namespace Cs.Hiding
{
    public interface A
    {
        object test();
    }

    public class B<T> : A
    {
        object A.test()
        {
            return null;
        }

        public T test()
        {
            return default(T);
        }
    }

    public static class Program
    {
        public static void Main()
        {
            List<object> list = null;
            object /*type:object*/str = list[0];
            string /*type:string*/fromB = new B<string>().test();
        }
    }
}
