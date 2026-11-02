global using System.Linq;
using System;
using static System.Math;
using Alias = System.Text.StringBuilder;

using var stream = Open();
using (var other = Open()) { }
int x = 1;
const int Y = 2;
Console.WriteLine(x + Y);
new Alias().Append(x);
static int Twice(int v) => v * 2;
async System.Threading.Tasks.Task Later() { }

partial class Partial { }
static class Helpers { }
public int Field;

namespace N
{
    class Inner { }
}
