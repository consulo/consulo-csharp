// sdk:NET_10
using System.Collections.Generic;
using System.Threading.Tasks;

class C
{
    IAsyncEnumerable<int> /*error:CS8403*/Q() { yield return 1; }
    async IAsyncEnumerable<int> R() { await Task.Yield(); yield return 1; }
    async IAsyncEnumerator<string> S() { await Task.Yield(); yield return "s"; }
}
