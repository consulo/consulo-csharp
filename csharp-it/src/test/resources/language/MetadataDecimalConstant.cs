// sdk:NET_10
// a const decimal of a library is a static readonly field with DecimalConstantAttribute
using Lib;

class Test
{
    const decimal Price = Prices.Price;
    const int Count = Prices.Count;
    const decimal Field = /*error:CS0133*/Prices.Field;

    void Method()
    {
        var /*type:decimal*/price = Prices.Price;
    }
}
