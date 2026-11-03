using System;

Console.WriteLine(First.Value);

public static class First
{
    public const int Value = 1;
}

/*error:CS8803*/Console.WriteLine(First.Value + 1);
