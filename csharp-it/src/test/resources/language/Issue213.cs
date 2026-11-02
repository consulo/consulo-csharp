using System;

namespace Cs.Lambdas
{
    public static class Program
    {
        public static void Main()
        {
            test(() =>
            {
                return "";
            });
            test(() => { });
            int /*type:int*/value = pick(() => 1);
            string /*type:string*/text = pick(() => "");
        }

        private static void test(Action actionString)
        {
            actionString();
        }

        private static void test(Func<string> action)
        {
            action();
        }

        private static int pick(Func<int> func)
        {
            return func();
        }

        private static string pick(Func<string> func)
        {
            return func();
        }
    }
}
