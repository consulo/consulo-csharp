namespace Cs.Conditionals
{
    public enum MethodAttributes
    {
        Static = 16
    }

    public class Shape
    {
    }

    public class Circle : Shape
    {
    }

    public class Square : Shape
    {
    }

    public static class Conditionals
    {
        public static Shape Pick(bool flag, int intValue, Circle circle, Square square)
        {
            MethodAttributes attributes = MethodAttributes.Static;

            int value = /*error:CS0173*/flag ? intValue : attributes;
            var inferred = /*error:CS0173*/flag ? intValue : attributes;
            object boxed = flag ? intValue : attributes;
            long widened = flag ? intValue : 1L;
            string text = flag ? "a" : null;
            Shape shape = flag ? circle : square;
            Shape upcast = flag ? circle : (Shape) square;
            Circle /*type:Cs.Conditionals.Circle*/created = flag ? new() : new Circle();
            System.Func<int, int> func = flag ? x => x : x => x + 1;
            return flag ? circle : square;
        }
    }
}
