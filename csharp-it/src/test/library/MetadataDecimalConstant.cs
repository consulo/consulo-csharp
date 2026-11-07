namespace Lib
{
    // DecimalConstantAttribute - a const decimal is a static readonly field with it, decimal is no constant of the CLR
    public static class Prices
    {
        public const decimal Price = 1.5m;
        public const int Count = 2;
        public static readonly decimal Field = 1.5m;
    }
}
