namespace Cs.Conversions
{
    public struct Vector2
    {
        public static Vector2 zero;

        public static Vector2 operator +(Vector2 a, Vector2 b) => a;
        public static Vector2 operator *(float d, Vector2 a) => a;

        public static implicit operator Vector3(Vector2 v) => new Vector3();
        public static implicit operator Vector2(Vector3 v) => new Vector2();
    }

    public struct Vector3
    {
    }

    // declared by the source type only
    public struct Meters
    {
        public static implicit operator Feet(Meters m) => new Feet();
    }

    public struct Feet
    {
    }

    public struct Color
    {
        public static implicit operator Color32(Color c) => new Color32(0, 0, 0, 0);
        public static implicit operator Color(Color32 c) => new Color();
    }

    public struct Color32
    {
        public Color32(byte r, byte g, byte b, byte a)
        {
        }
    }

    public class Test
    {
        void Whatever()
        {
            Vector3 v3;
            Vector3 pos;
            Vector2 v2 = Vector2.zero;
            v3 = v2;
            v3 = v2;
            v3 = v2 + (2 * v2);
            v2 = v3;
            pos = v2 + (2 * v2);
            v3 = v2 + (2 * v2);
            v3 = v2;

            ThatV3(v2);
            var sum = /*type:Cs.Conversions.Vector2*/v2 + v2;
            ThatV3(sum);
            ThatV3(v2 + v2);

            Color c;
            Color32 c32 = new Color32(0, 0, 0, 0);
            c = c32;
            Color cc;
            cc = c32;
            c32 = cc;
        }

        void ThatV3(Vector3 pv3)
        {
        }

        void Lengths(Meters meters)
        {
            Feet feet = meters;
            TakeFeet(meters);
        }

        void TakeFeet(Feet feet)
        {
        }

        void ByteTests()
        {
            byte b = 0;
            b = /*error:CS0029*/b | 1;
            b = (byte) (b | 1);
            b |= 1;
        }
    }
}
