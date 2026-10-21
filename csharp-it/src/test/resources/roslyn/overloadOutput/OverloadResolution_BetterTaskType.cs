// expectedOutput: 1
using System;
using System.Threading.Tasks;
class P
{
  static void M1(Task<int> a) { Console.Write(1); }
  static void M1(Task<uint> a) { Console.Write(2); }

  static void Main()
  {
    M1(null);
  }
}