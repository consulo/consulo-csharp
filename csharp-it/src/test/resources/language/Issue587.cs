using System;

namespace Cs.Refs
{
    public sealed class Resource : IDisposable
    {
        public int Size;

        public void Dispose() { }
    }

    public delegate int InHandler(in int value);

    public static class UsingDeclarations
    {
        public static int Run()
        {
            using var /*type:Cs.Refs.Resource*/resource = new Resource();
            using Resource other = new Resource(), /*type:Cs.Refs.Resource*/another = new Resource();

            InHandler handler = (in int value) => value + 1;

            return resource.Size + other.Size + another.Size + handler(1);
        }
    }
}
