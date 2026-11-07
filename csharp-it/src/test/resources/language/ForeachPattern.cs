// the element type of foreach by the pattern - GetEnumerator() with Current, without IEnumerable<T>
class Numbers
{
    public Enumerator GetEnumerator() => new Enumerator();

    public struct Enumerator
    {
        public int Current => 0;

        public bool MoveNext() => false;
    }
}

class Test
{
    void Method()
    {
        foreach (var /*type:int*/item in new Numbers())
        {
        }
    }
}
