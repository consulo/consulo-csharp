using System;

namespace Cs.Scopes
{
    public static class Redeclarations
    {
        public static void Siblings(string[] items)
        {
            for (int i = 0; i < 2; i++)
            {
            }
            for (int i = 0; i < 3; i++)
            {
            }
            foreach (string item in items)
            {
            }
            foreach (string item in items)
            {
            }
            {
                int local = 1;
            }
            {
                int local = 2;
            }
        }

        public static void Nested(int value, string[] items)
        {
            {
                int /*error:CS0136*/value = 1;
            }
            foreach (string item in items)
            {
                if (item != null)
                {
                    int /*error:CS0136*/pos = 1;
                }
            }
            int pos = 2;
            for (int /*error:CS0136*/pos2 = 0; pos2 < 1; pos2++)
            {
            }
            int pos2 = 3;
        }

        public static void Functions(int value)
        {
            int pos = 1;
            Action action = () =>
            {
                int pos = 2;
                int value = 3;
            };
            Func<int, int> func = value => value + pos;

            int Local(int pos)
            {
                return pos;
            }
        }
    }
}
