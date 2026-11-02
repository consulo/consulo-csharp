namespace Cs.Refs
{
    public struct Counter
    {
        public int Value;

        public static void Increment(ref Counter counter)
        {
            counter.Value++;
        }

        public void IncrementSelf()
        {
            Increment(ref this);
        }
    }

    public class Holder
    {
        public static void Use(ref Holder holder)
        {
        }

        public void UseSelf()
        {
            Use(ref /*error:CS1510*/this);
        }
    }
}
