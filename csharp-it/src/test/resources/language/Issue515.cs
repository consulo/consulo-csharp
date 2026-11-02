namespace Cs.Compound
{
    public static class Assignments
    {
        public static void Run(int i, long l, double d, string text)
        {
            short s = 0;
            s += 1;
            /*error:CS0029*/s -= 'a';
            /*error:CS0029*/s -= i;
            /*error:CS0029*/s *= l;

            byte b = 0;
            b |= 1;
            b <<= 2;
            /*error:CS0029*/b += i;

            int n = 0;
            n += i;
            n += 'c';
            /*error:CS0029*/n += l;
            /*error:CS0029*/n *= d;

            long total = 0;
            total += i;
            total += n;

            float f = 0;
            f += i;
            /*error:CS0029*/f += d;

            text += i;
            text += null;

            System.Action action = null;
            action += Run2;
            action -= Run2;
        }

        private static void Run2()
        {
        }
    }
}
