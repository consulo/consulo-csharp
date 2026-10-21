enum Color
{
    Color, //inside the decl, this has type int so there is no Color Color Issue
    Blue,
    Navy = /*@symbol:Field Color Color.Color*/Color.Blue,
}
