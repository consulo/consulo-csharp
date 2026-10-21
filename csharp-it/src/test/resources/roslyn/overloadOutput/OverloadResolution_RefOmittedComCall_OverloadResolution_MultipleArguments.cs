// expectedOutput: 1\n1\n1\n1\n1\n1\n2\n2\n\n4\n3\n4\n3\n4\n4\n\n6\n5\n6\n6\n6\n5\n5\n6\n6\n\n7\n7\n8\n8\n7\n7\n7\n8\n\n10\n9\n10\n10\n10\n9\n9\n10\n10\n\n12\n12\n11\n11\n12\n12\n\n13\n14\n14\n13\n14\n14\n14\n13\n13\n13\n\n16\n15\n16\n15\n15\n15\n16\n16\n\n18\n17\n17\n18\n17\n17\n17\n18\n18\n18\n18\n18\n17\n17\n18\n17
using System;
using System.Runtime.InteropServices;

[ComImport]
[Guid("A88A175D-2448-447A-B786-64682CBEF156")]
public interface IRef1
{
    void M1(int x, long y);
    void M1(ref int x, int y);

    void M2(int x, char y);
    void M2(ref int x, int y);

    void M3(ref int x, char y);
    void M3(ref long x, int y);

    void M4(ref int x, long y);
    void M4(ref int x, ref int y);

    void M5(ref int x, char y);
    void M5(ref long x, int y);

    void M6(ref int x, int y);
    void M6(ref long x, int y);

    void M7(ref int x, long y);
    void M7(ref long x, ref int y);

    void M8(long x, ref int y);
    void M8(ref long x, int y);

    void M9(ref long x, ref int y);
    void M9(ref int x, ref long y);
}

public class Ref1Impl : IRef1
{
    public void M1(int x, long y) { Console.WriteLine(1); }
    public void M1(ref int x, int y) { Console.WriteLine(2); }

    public void M2(int x, char y) { Console.WriteLine(3); }
    public void M2(ref int x, int y) { Console.WriteLine(4); }

    public void M3(ref int x, char y) { Console.WriteLine(5); }
    public void M3(ref long x, int y) { Console.WriteLine(6); }

    public void M4(ref int x, long y) { Console.WriteLine(7); }
    public void M4(ref int x, ref int y) { Console.WriteLine(8); }

    public void M5(ref int x, char y) { Console.WriteLine(9); }
    public void M5(ref long x, int y) { Console.WriteLine(10); }

    public void M6(ref int x, int y) { Console.WriteLine(11); }
    public void M6(ref long x, int y) { Console.WriteLine(12); }

    public void M7(ref int x, long y) { Console.WriteLine(13); }
    public void M7(ref long x, ref int y) { Console.WriteLine(14); }

    public void M8(long x, ref int y) { Console.WriteLine(15); }
    public void M8(ref long x, int y) { Console.WriteLine(16); }

    public void M9(ref long x, ref int y) { Console.WriteLine(17); }
    public void M9(ref int x, ref long y) { Console.WriteLine(18); }
}

class Test
{
   public static void Main()
   {
       IRef1 ref1 = new Ref1Impl();
       int i = 1;
       long l = 1;
       char c = 'c';


//     void M1(int x, long y);
//     void M1(ref int x, int y);

       ref1.M1(i, i);
       ref1.M1(i, l);
       ref1.M1(i, c);
       //ref1.M1(l, i);      CS1503
       //ref1.M1(l, l);      CS1503
       //ref1.M1(l, c);      CS1503
       ref1.M1(c, i);
       ref1.M1(c, l);
       ref1.M1(c, c);

       ref1.M1(ref i, i);
       //ref1.M1(ref i, l);  CS1615
       ref1.M1(ref i, c);
       //ref1.M1(ref l, i);  CS1615
       //ref1.M1(ref l, l);  CS1615
       //ref1.M1(ref l, c);  CS1615
       //ref1.M1(ref c, i);  CS1615
       //ref1.M1(ref c, l);  CS1615
       //ref1.M1(ref c, c);  CS1615


//     void M2(int x, char y);
//     void M2(ref int x, int y);

       Console.WriteLine();
       ref1.M2(i, i);
       //ref1.M2(i, l);      CS1503
       ref1.M2(i, c);
       //ref1.M2(l, i);      CS1503, CS1503
       //ref1.M2(l, l);      CS1503, CS1503
       //ref1.M2(l, c);      CS1503
       ref1.M2(c, i);
       //ref1.M2(c, l);      CS1503
       ref1.M2(c, c);

       ref1.M2(ref i, i);
       //ref1.M2(ref i, l);  CS1615, CS1503
       ref1.M2(ref i, c);
       //ref1.M2(ref l, i);  CS1615, CS1503
       //ref1.M2(ref l, l);  CS1615, CS1503
       //ref1.M2(ref l, c);  CS1615
       //ref1.M2(ref c, i);  CS1615, CS1503
       //ref1.M2(ref c, l);  CS1615, CS1503
       //ref1.M2(ref c, c);  CS1615


//     void M3(ref int x, char y);
//     void M3(ref long x, int y);

       Console.WriteLine();
       ref1.M3(i, i);
       //ref1.M3(i, l);      CS1503
       ref1.M3(i, c);
       ref1.M3(l, i);
       //ref1.M3(l, l);      CS1620, CS1503
       ref1.M3(l, c);
       ref1.M3(c, i);
       //ref1.M3(c, l);      CS1503
       ref1.M3(c, c);

       //ref1.M3(ref i, i);  CS1503
       //ref1.M3(ref i, l);  CS1503
       ref1.M3(ref i, c);
       ref1.M3(ref l, i);
       //ref1.M3(ref l, l);  CS1503, CS1503
       ref1.M3(ref l, c);
       //ref1.M3(ref c, i);  CS1503, CS1503
       //ref1.M3(ref c, l);  CS1503, CS1503
       //ref1.M3(ref c, c);  CS1503


//     void M4(ref int x, long y);
//     void M4(ref int x, ref int y);

       Console.WriteLine();
       //ref1.M4(i, i);      CS0121
       ref1.M4(i, l);
       //ref1.M4(i, c);      CS0121
       //ref1.M4(l, i);      CS1620
       //ref1.M4(l, l);      CS1620
       //ref1.M4(l, c);      CS1620
       //ref1.M4(c, i);      CS0121
       ref1.M4(c, l);      
       //ref1.M4(c, c);      CS0121

       ref1.M4(i, ref i);
       //ref1.M4(l, ref i);  CS1620, CS1615
       ref1.M4(c, ref i);
       //ref1.M4(i, ref l);  CS1615
       //ref1.M4(l, ref l);  CS1620
       //ref1.M4(c, ref l);  CS1615

       ref1.M4(ref i, i);
       ref1.M4(ref i, l);
       ref1.M4(ref i, c);

       ref1.M4(ref i, ref i);


//     void M5(ref int x, char y);
//     void M5(ref long x, int y);

       Console.WriteLine();
       ref1.M5(i, i);
       //ref1.M5(i, l);    CS1503
       ref1.M5(i, c);
       ref1.M5(l, i);
       //ref1.M5(l, l);    CS1620, CS1503
       ref1.M5(l, c);
       ref1.M5(c, i);
       //ref1.M5(c, l);    CS1503
       ref1.M5(c, c);

       //ref1.M5(ref i, i);  CS1503
       //ref1.M5(ref i, l);  CS1503
       ref1.M5(ref i, c);
       ref1.M5(ref l, i);
       //ref1.M5(ref l, l);  CS1503
       ref1.M5(ref l, c);


//     void M6(ref int x, int y);
//     void M6(ref long x, int y);

       Console.WriteLine();
       //ref1.M6(i, i);    CS0121
       //ref1.M6(i, l);    CS1503
       //ref1.M6(i, c);    CS0121
       ref1.M6(l, i);
       //ref1.M6(l, l);    CS1620, CS1503
       ref1.M6(l, c);
       //ref1.M6(c, i);    CS0121
       //ref1.M6(c, l);    CS1503
       //ref1.M6(c, c);    CS0121

       ref1.M6(ref i, i);
       //ref1.M6(ref i, l);  CS1503
       ref1.M6(ref i, c);
       ref1.M6(ref l, i);
       //ref1.M6(ref l, l);  CS1503, CS1503
       ref1.M6(ref l, c);


//     void M7(ref int x, long y);
//     void M7(ref long x, ref int y);

       Console.WriteLine();
       //ref1.M7(i, i);    CS0121
       ref1.M7(i, l);
       //ref1.M7(i, c);    CS0121
       ref1.M7(l, i);
       //ref1.M7(l, l);    CS1620
       ref1.M7(l, c);
       //ref1.M7(c, i);    CS0121
       ref1.M7(c, l);
       //ref1.M7(c, c);    CS0121

       ref1.M7(i, ref i);
       ref1.M7(l, ref i);
       ref1.M7(c, ref i);
       //ref1.M7(i, ref l);  CS1615
       //ref1.M7(l, ref l);  CS1620, CS1615
       //ref1.M7(c, ref l);  CS1615

       ref1.M7(ref i, i);
       ref1.M7(ref i, l);
       ref1.M7(ref i, c);

       //ref1.M7(ref i, ref i);  CS1615


//     void M8(long x, ref int y);
//     void M8(ref long x, int y);

       Console.WriteLine();
       ref1.M8(i, i);
       //ref1.M8(i, l);    CS1620
       //ref1.M8(i, c);    CS0121
       //ref1.M8(l, i);    CS0121
       //ref1.M8(l, l);    CS1620
       ref1.M8(l, c);
       ref1.M8(c, i);
       //ref1.M8(c, l);    CS1620
       //ref1.M8(c, c);    CS0121

       ref1.M8(i, ref i);
       ref1.M8(l, ref i);
       ref1.M8(c, ref i);
       //ref1.M8(i, ref l);   CS1503
       //ref1.M8(l, ref l);   CS1503
       //ref1.M8(c, ref l);   CS1503

       //ref1.M8(ref i, i);   CS1615
       //ref1.M8(ref i, l);   CS1615, CS1620
       //ref1.M8(ref i, c);   CS1615
       ref1.M8(ref l, i);
       //ref1.M8(ref l, l);   CS1615, CS1620
       ref1.M8(ref l, c);

       //ref1.M8(ref i, ref i);   CS1615
       //ref1.M8(ref i, ref l);   CS1615, CS1503
       //ref1.M8(ref l, ref i);   CS1615
       //ref1.M8(ref l, ref l);   CS1615, CS1503


//     void M9(ref long x, ref int y);
//     void M9(ref int x, ref long y);

       Console.WriteLine();
       //ref1.M9(i, i);    CS0121
       ref1.M9(i, l);
       //ref1.M9(i, c);    CS0121
       ref1.M9(l, i);
       //ref1.M9(l, l);    CS1620
       ref1.M9(l, c);
       //ref1.M9(c, i);    CS0121
       ref1.M9(c, l);
       //ref1.M9(c, c);    CS0121

       ref1.M9(i, ref i);
       ref1.M9(l, ref i);
       ref1.M9(c, ref i);
       ref1.M9(i, ref l);
       //ref1.M9(l, ref l);   CS1503
       ref1.M9(c, ref l);

       ref1.M9(ref i, i);
       ref1.M9(ref i, l);
       ref1.M9(ref i, c);
       ref1.M9(ref l, i);
       //ref1.M9(ref l, l);   CS1620
       ref1.M9(ref l, c);

       //ref1.M9(ref i, ref i);   CS1503
       ref1.M9(ref i, ref l);
       ref1.M9(ref l, ref i);
       //ref1.M9(ref l, ref l);   CS1503
   }
}
