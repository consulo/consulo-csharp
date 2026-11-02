namespace Cs.ImplicitCast
{
    public class BBParameter<T>
    {
        public static implicit operator BBParameter<T>(T value)
        {
            return new BBParameter<T>();
        }
    }

    public class TestMe
    {
        private BBParameter<float> test = 1;
        private BBParameter<double> other = 1.5f;
        private BBParameter<string> text = "a";
    }
}
