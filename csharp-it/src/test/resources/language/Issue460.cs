// sdk:NET_10
using System.Threading.Tasks;

namespace Cs.Async
{
    public static class ValueTasks
    {
        public static async ValueTask<int> Count()
        {
            return 42;
        }

        public static async ValueTask Run()
        {
            int value = /*type:int*/await Count();
            await Run();
        }
    }
}
