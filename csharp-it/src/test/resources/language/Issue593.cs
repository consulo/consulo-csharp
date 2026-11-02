namespace Cs.News
{
    public class Point
    {
        public int X;

        public Point() { }

        public Point(int x) { X = x; }
    }

    public class Box<T>
    {
        public T Value;
    }

    public class Holder
    {
        private Point myOrigin = /*type:Cs.News.Point*/new();
        private Box<string> myBox = /*type:Cs.News.Box<string>*/new();
        private (int, string)[] myPairs = new (int, string)[2];

        public Point Create()
        {
            Point local = /*type:Cs.News.Point*/new(1);
            var /*type:Cs.News.Point*/copy = local;
            myOrigin = (/*type:Cs.News.Point*/new(2));
            return /*type:Cs.News.Point*/new() { X = 3 };
        }

        public int Length() => myBox.Value.Length + myOrigin.X + myPairs.Length;
    }
}
