using System;

namespace B
{
    public interface S<T>
    {
        void test();

        int Test
        {
            get;
        }
    }

    public abstract class B<S>
    {}

    public class SImpl : S<String>
    {
        int S<String>.Test
        {
            get
            {
                return 1;
            }
        }

        void S<String>.test()
        {

        }
    }
}
