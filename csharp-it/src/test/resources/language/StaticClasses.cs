using System.Collections.Generic;

interface I { }
class Base { }

static class S1
{
    /*error:CS0710*/S1() { }
    static S1() { }
    ~/*error:CS0711*/S1() { }
    public static int operator /*error:CS0715*/+(/*error:CS0721*/S1 a, /*error:CS0721*/S1 b) => 0;
    public static implicit operator /*error:CS0715*/int(/*error:CS0721*/S1 a) => 0;
    public int /*error:CS0720*/this[int i] => i;
}

static class S2 : /*error:CS0713*/Base { }

static class S3 : /*error:CS0714*/I { }

static class S4 : object { }

class G<T> where T : /*error:CS0717*/S4 { }

class Use
{
    void M(object o)
    {
        /*error:CS0723*/var s = /*error:CS0712*/new S4();
        var list = new List</*error:CS0718*/S4>();
        /*error:CS0719*/S4[] arr = null;
        /*error:CS0723*/var c = /*error:CS0716*/(S4) o;
        var t = typeof(S4);
    }
}
