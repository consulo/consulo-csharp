namespace Cs.Overloads
{
    public class Temp
    {
        public void Foo()
        {
            int a = /*type:int*/f(1, 2);
            int b = /*type:int*/f(1, (short) 2);
            int c = /*type:int*/f((short) 1, (short) 2);
            float d = /*type:float*/f(1f, 2);
        }

        public static float f(float a, float b)
        {
            return a + b;
        }

        public static int f(int a, int b)
        {
            return a + b;
        }
    }
}
