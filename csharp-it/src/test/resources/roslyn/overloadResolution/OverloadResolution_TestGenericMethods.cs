
class C 
{ 
    class D<T> 
    { 
        public static void N<U>(U u){} 
        public class E<V>  
        {
            public static void O<W>(W w){}
        }
    }
    void M()
    {
        D<int>.N<byte>(1); //-C.D<int>.N<byte>(byte)
        D<int>.E<double>.O<short>(1); //-C.D<int>.E<double>.O<short>(short)
    }
}