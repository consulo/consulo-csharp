// expectedOutput: 1\n1\n1\n1\n2\n\n3\n3\n3\n3\n3\n3\n4\n\n6\n5\n6\n5\n6\n\n8\n8\n8\n8\n8\n\n10\n10\n9\n10\n\n12\n12\n12\n12\n12\n11\n\n14\n13\n14\n14\n13\n14\n13\n\n15\n15\n16\n15\n15\n16\n15\n\n18\n18\n18\n18\n18\n18\n17
using System;
using System.Runtime.InteropServices;

[ComImport]
[Guid("A88A175D-2448-447A-B786-64682CBEF156")]
public interface IRef1
{
    void M1(int x);
    void M1(ref int x);

    void M2(long x);
    void M2(ref int x);

    void M3(char x);
    void M3(ref int x);

    void M4(out uint x);
    void M4(ref int x);

    void M5(ref int x);
    void M5(ref long x);

    void M6(ref char x);
    void M6(ref long x);

    void M7(ref long x);
    void M7(int x);

    void M8(ref long x);
    void M8(char x);

    void M9(ref char x);
    void M9(long x);
}

public class Ref1Impl : IRef1
{
    public void M1(int x) { Console.WriteLine(1); }
    public void M1(ref int x) { Console.WriteLine(2); }

    public void M2(long x) { Console.WriteLine(3); }
    public void M2(ref int x) { Console.WriteLine(4); }

    public void M3(char x) { Console.WriteLine(5); }
    public void M3(ref int x) { Console.WriteLine(6); }

    public void M4(out uint x) { x = 0; Console.WriteLine(7); }
    public void M4(ref int x) { Console.WriteLine(8); }

    public void M5(ref int x) { Console.WriteLine(9); }
    public void M5(ref long x) { Console.WriteLine(10); }

    public void M6(ref char x) { Console.WriteLine(11); }
    public void M6(ref long x) { Console.WriteLine(12); }

    public void M7(ref long x) { Console.WriteLine(13); }
    public void M7(int x) { Console.WriteLine(14); }

    public void M8(ref long x) { Console.WriteLine(15); }
    public void M8(char x) { Console.WriteLine(16); }

    public void M9(ref char x) { Console.WriteLine(17); }
    public void M9(long x) { Console.WriteLine(18); }
}

class Test
{
   public static void Main()
   {
       IRef1 ref1 = new Ref1Impl();
       int i = 1;
       long l = 1;
       char c = 'c';

//     void M1(int x);
//     void M1(ref int x);

       ref1.M1(10);
       //ref1.M1(10L);      CS1503
       ref1.M1('c');
       ref1.M1(i);
       //ref1.M1(l);        CS1503
       ref1.M1(c);
       ref1.M1(ref i);
       //ref1.M1(ref l);    CS1615
       //ref1.M1(ref c);    CS1615


//     void M2(long x);
//     void M2(ref int x);

       Console.WriteLine();
       ref1.M2(10);
       ref1.M2(10L);
       ref1.M2('c');
       ref1.M2(i);
       ref1.M2(l);
       ref1.M2(c);
       ref1.M2(ref i);
       //ref1.M2(ref l);    CS1615
       //ref1.M2(ref c);    CS1615


//     void M3(char x);
//     void M3(ref int x);

       Console.WriteLine();
       ref1.M3(10);
       //ref1.M3(10L);      CS1503
       ref1.M3('c');
       ref1.M3(i);
       //ref1.M3(l);        CS1503
       ref1.M3(c);
       ref1.M3(ref i);
       //ref1.M3(ref l);    CS1615
       //ref1.M3(ref c);    CS1615


//     void M4(out uint x);
//     void M4(ref int x);

       Console.WriteLine();
       ref1.M4(10);
       //ref1.M4(10L);      CS1620
       ref1.M4('c');
       ref1.M4(i);
       //ref1.M4(l);        CS1620
       ref1.M4(c);
       ref1.M4(ref i);
       //ref1.M4(ref l);    CS1620
       //ref1.M4(ref c);    CS1620


//     void M5(ref int x);
//     void M5(ref long x);

       Console.WriteLine();
       //ref1.M5(10);       CS0121
       ref1.M5(10L);
       //ref1.M5('c');      CS0121
       //ref1.M5(i);        CS0121
       ref1.M5(l);
       //ref1.M5(c);        CS0121
       ref1.M5(ref i);
       ref1.M5(ref l);
       //ref1.M5(ref c);    CS1503


//     void M6(ref char x);
//     void M6(ref long x);

       Console.WriteLine();
       ref1.M6(10);
       ref1.M6(10L);
       //ref1.M6('c');      CS0121
       ref1.M6(i);
       ref1.M6(l);
       //ref1.M6(c);        CS0121
       //ref1.M6(ref i);    CS1503   
       ref1.M6(ref l);
       ref1.M6(ref c);


//     void M7(ref long x);
//     void M7(int x);

       Console.WriteLine();
       ref1.M7(10);
       ref1.M7(10L);
       ref1.M7('c');
       ref1.M7(i);
       ref1.M7(l);
       ref1.M7(c);
       //ref1.M7(ref i);    CS1503
       ref1.M7(ref l);
       //ref1.M7(ref c);    CS1503


//     void M8(ref long x);
//     void M8(char x);

       Console.WriteLine();
       ref1.M8(10);
       ref1.M8(10L);
       ref1.M8('c');
       ref1.M8(i);
       ref1.M8(l);
       ref1.M8(c);
       //ref1.M8(ref i);    CS1503
       ref1.M8(ref l);
       //ref1.M8(ref c);    CS1503


//     void M9(ref char x);
//     void M9(long x);

       Console.WriteLine();
       ref1.M9(10);
       ref1.M9(10L);
       ref1.M9('c');
       ref1.M9(i);
       ref1.M9(l);
       ref1.M9(c);
       //ref1.M9(ref i);    CS1503
       //ref1.M9(ref l);    CS1503
       ref1.M9(ref c);
   }
}
