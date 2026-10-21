// expectedOutput: 13
using System;

class P
{
  static void M1(Func<int> a) { Console.Write(1); }
  static void M1(Func<uint> a) { Console.Write(2); }
  static void M2(Func<int> a) { Console.Write(3); }
  static void M2(Action a) { Console.Write(4); }

  static void Main()
  {
    M1(null);
    M2(null);
  }
}