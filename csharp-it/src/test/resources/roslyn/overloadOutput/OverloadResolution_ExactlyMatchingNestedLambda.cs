// expectedOutput: 1
using System;
class P
{
  delegate Func<int> DA();    
  delegate Func<object> DB();    

  static void M1(DA a) { Console.Write(1); }
  static void M1(DB a) { Console.Write(2); }

  static void Main()
  {
    int i = 0;
    
    M1(() => () => i);
  }
}