
delegate void Action();
delegate void Action<in A>(A a);
delegate R Func<out R>();
delegate R Func<in A, out R>(A a);
delegate R Func2<in A, out R>(A a);

class Animal {}
class Mammal : Animal {}
class Tiger : Mammal {}

class C 
{ 
    static void N1(Func<object> f){}
    static void N1(Func<string> f){}

    static void N2(Func<int, object> f){}
    static void N2(Action<int> f){}

    static void N3(Func<int, Tiger> f){}
    static void N3(Func2<int, Mammal> f){}

    static void N4(Func<int, Animal> f){}
    static void N4(Func2<int, Mammal> f){}


    void M() 
    { 
        // If we have a lambda argument and two delegates, the rules are:

        // First, if one delegate is convertible to the other but not vice-versa
        // then the more specific delegate wins. A Func<string> is convertible to Func<object>
        // but not vice-versa, so Func<string> must be more specific:

        // This test is disabled; see the comments to TestBug11961 above.
        // N1(()=>null); 

        // Second, if the delegates have identical parameters, then the non-void one wins.
        // This lambda could be both an action and a func; we don't know if the construction
        // is being done for its value or for its side effects. 

        N2(x=>new System.Object()); //-C.N2(Func<int, object>)

        // Third, if the delegates have identical parameters and both have a return type
        // and the lambda has an inferred return type, then the better delegate is the
        // one where the delegate return type exactly matches the lambda return type.

        N3(x=>new Tiger()); //-C.N3(Func<int, Tiger>)

        // Fourth, if the delegate return type does not exactly match the lambda return
        // type then the most specific delegate return type wins.

        N4(x=>new Tiger()); //-C.N4(Func2<int, Mammal>)
    }
}
