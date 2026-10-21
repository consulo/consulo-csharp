using System; 

enum Color { Red }
class Bravo { public static Color Red { get; set; } }
class C
{
    static void X(Action<Color> f) {}
    static void X(Action<Bravo> f) { }
    static void Main()
    {
        X(Color => Console.WriteLine("{0}{1}", /*@symbol:Type Color*/Color/*@parent:Field Color Color.Red*/.Red, Color.ToString()));
    }
}
