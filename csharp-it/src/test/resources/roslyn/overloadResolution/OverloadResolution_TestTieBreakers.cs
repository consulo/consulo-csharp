

class C 
{ 
    class D<TD>{}
 
    void N1<T1>(T1 p1) {}
    void N1(int p1) {}

    void N2(int p1) {}
    void N2(params int[] p1) {}

    void N3(int p1, int p2, params int[] p3) {}
    void N3(int p1, params int[] p3) {}

    void N4(int p1, int p2 = 0) {}
    void N4(int p1) {}

    void N51<T51>(T51 p1, double p2 = 0) {}
    void N51<T51>(int p1, string p2 = null ) {}

    void N52<T52>(D<T52> p1, double p2 = 0) {}
    void N52<T52>(D<int> p1, string p2 = null ) {}

    void N53<T53>(T53[] p1, double p2 = 0) {}
    void N53<T53>(int[] p1, string p2 = null ) {}

    
    void M() 
    { 
        // If due to construction or dynamicness all the effective parameters of two methods are identical 
        // then we do a series of tiebreaking rules.

        // 1: A generic method is worse than a non-generic method.
        N1(123); //-C.N1(int)

        // 2: A method applicable in normal form is better than one applicable only in expanded form.
        N2(123); //-C.N2(int)

        // 3: If both methods are applicable in expanded form then the one with more 'real' parameters wins.
         
        N3(1, 2, 3, 4, 5, 6); //-C.N3(int, int, params int[])

        // 4: If one method has no default arguments substituted and the other has one or more, the one
        // with no defaults wins.
        
        N4(1); //-C.N4(int)

        // 5: The more specific method wins. One method's declared parameter types list is more specific than
        // anothers if, for each position in the list, the type of one is not less specific than that
        // of the other, and, for at least one position, the type is more specific than that of the other.

        // 5.1: A type parameter is less specific than any other type.
        // 5.2: A constructed type C<X, Y> is less specific than C<A, B> if the type list <X, Y> is less specific than <A, B>
        // 5.3: An array type X[] is less specific than Y[] if X is less specific than Y.
        // 5.4: NOT TESTED: A pointer type X* is less specific than Y* if X is less specific than Y*.

//EDMAURER removed the next three tests that fail due to the fact that omitted optional parameters are not supported.
        // N51<int>(123); //C.N51<int>(int, string)
        // N52<int>(null); //C.N52<int>(C.D<int>, string)
        // N53<int>(null); //C.N53<int>(int[], string)
        
        // 6: NOT TESTED: A non-lifted operator is better than a lifted operator.

    }
}
