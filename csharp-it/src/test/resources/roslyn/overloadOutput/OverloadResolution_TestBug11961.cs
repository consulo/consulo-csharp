// expectedOutput: 232579D
using System;
class P
{
  static void M(Action<string> a) { Console.Write(1); }
  static void M(Action<object> a) { Console.Write(2); }
  static void M(string x, Action<string> a) { Console.Write(3); }
  static void M(object x, Action<object> a) { Console.Write(4); }
  static void M1(string x, Func<object> a) { Console.Write(5); }
  static void M1(object x, Func<ValueType> a) { Console.Write(6); }
  static void M2(Func<object> a, string x) { Console.Write(7); }
  static void M2(Func<ValueType> a, object x) { Console.Write(8); }
  static void M3(Func<object> a, Action<object> b, string x) { Console.Write(9); }
  static void M3(Func<ValueType> a, Action<string> b,  object x) { Console.Write('A'); }
  static void M5(Action<object> b, string x, Func<object> a) { Console.Write('D'); }
  static void M5(Action<string> b, object x, Func<ValueType> a) { Console.Write('E'); }
  static void Main()
  {
    M(null);
    M((string)null, q=>{});
    M(q=>{});
    M1((string)null, ()=>{ throw new NotImplementedException();});
    M2(()=>{ throw new NotImplementedException();}, (string)null);
    M3(()=>{ throw new NotImplementedException();}, q=> {}, (string)null);
    M5(q=> {}, (string)null, ()=>{ throw new NotImplementedException();});
  }
}