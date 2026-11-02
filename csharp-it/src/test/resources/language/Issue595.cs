using System;

namespace Cs.Operators
{
    public static class Arithmetic
    {
        public static void Run()
        {
            double current = 0;
            current += 100000;
            current *= 0.9;
            long testLong = 1234;
            testLong += 420;
            testLong /= 13;
            testLong++;
            int t = 13;
            t++;
            t += 406;
            double /*type:double*/mixed = current + testLong + t;
            long /*type:long*/widened = testLong * t;
            Console.WriteLine(current);
            Console.WriteLine(testLong);
            Console.WriteLine(t);
        }
    }
}
