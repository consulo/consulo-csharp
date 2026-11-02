namespace Cs.Patterns
{
    public class Circle
    {
        public double Radius;
    }

    public static class IsPatterns
    {
        public static int Check(object o, int number, string text, Circle circle)
        {
            int result = 0;
            if(o is null) result++;
            if(o is not null) result++;
            if(text is { Length: > 0 }) result++;
            if(o is Circle { Radius: > 1 } /*type:Cs.Patterns.Circle*/big) result += (int) big.Radius;
            if(circle is { Radius: var radius }) result += (int) radius;
            if(number is > 0 and < 10) result++;
            if(o is int or long) result++;
            if(o is string /*type:string*/str && str.Length > 1) result++;
            bool isNotCircle = /*type:bool*/o is not Circle;
            return isNotCircle ? result : 0;
        }
    }
}
