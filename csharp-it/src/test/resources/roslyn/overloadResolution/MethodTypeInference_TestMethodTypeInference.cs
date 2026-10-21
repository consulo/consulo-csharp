
using System.Collections.Generic;

interface Con<in CON> {}
interface Cov<out COV> {}
interface Inv<INV> {}
delegate void DCon<in DCON>();
delegate void DCov<out DCOV>();
delegate void DInv<DINV>();

class Animal {}
class Mammal : Animal {}
class Tiger : Mammal {}

delegate int FInt<FINT>(FINT p1);
delegate FUNC0 Func0<FUNC0>();
delegate FUNC1R Func1<FUNC1A, FUNC1R>(FUNC1A p1);

class Class<CLASS> { public enum Enum {} }
class Derived<DER> : Class<DER>, Con<DER>, Inv<DER>, Cov<DER> {}
struct Struct<STRUCT> : Con<STRUCT>, Inv<STRUCT>, Cov<STRUCT> {}
interface Interface<INTERFACE> : Con<INTERFACE>, Inv<INTERFACE>, Cov<INTERFACE> {}

class C 
{ 
    void N1<T1>(FInt<T1> p1){}

    void N32<R32>(Func0<R32> p1){}
    void N32<A32, R32>(A32 p1, Func1<A32, R32> p2){}

    int M32() { return 1; }
    int M32(string s) { return 1; }

    void N41<T41>(Con<T41> p1, ref T41 p2) {}
    void N42<T42>(Con<T42> p1, ref T42[] p2) {}
    void N431<T431>(Con<T431> p1, ref Class<T431> p2) {}
    void N432<T432>(Con<T432> p1, ref Struct<T432> p2) {}
    void N4321<T4321>(Con<T4321> p1, ref Struct<T4321>? p2) {}
    void N4322<T4322>(Con<T4322> p1, ref Class<T4322>.Enum p2) {}
    void N4331<T4331>(Con<T4331> p1, ref DCov<T4331> p2) {}
    void N4332<T4332>(Con<T4332> p1, ref DCon<T4332> p2) {}
    void N4333<T4333>(Con<T4333> p1, ref DInv<T4333> p2) {}
    void N4341<T4341>(Con<T4341> p1, ref Cov<T4341> p2) {}
    void N4342<T4342>(Con<T4342> p1, ref Con<T4342> p2) {}
    void N4343<T4343>(Con<T4343> p1, ref Inv<T4343> p2) {}

    void N51<T51>(T51 p1, T51 p2) {}
    void N5211<T5211>(T5211 p1, T5211[] p2) {}
    void N5212<T5212>(Con<T5212> p1, Struct<T5212>[] p2){}

    void N5231A<T5231A>(T5231A p1, IEnumerable<T5231A> p2) {}
    void N5231B<T5231B>(T5231B p1, ICollection<T5231B> p2) {}
    void N5231C<T5231C>(T5231C p1, IList<T5231C> p2) {}
    void N5231D<T5231D>(T5231D p1, IReadOnlyCollection<T5231D> p2) {}
    void N5231E<T5231E>(T5231E p1, IReadOnlyList<T5231E> p2) {}
    void N5232A<T5232A>(IEnumerable<T5232A> p1) {}
    void N5232B<T5232B>(ICollection<T5232B> p1) {}
    void N5232C<T5232C>(IList<T5232C> p1) {}
    void N5232D<T5232D>(IReadOnlyCollection<T5232D> p1) {}
    void N5232E<T5232E>(IReadOnlyList<T5232E> p1) {}

    void N5321<T5321>(Con<T5321> p1, Class<T5321> p2) {}
    void N5322<T5322>(Con<T5322> p1, Struct<T5322> p2) {}
    void N5323<T5323>(Con<T5323> p1, Struct<T5323>? p2) {}
    void N5331<T5331>(T5331 p1, DCov<T5331> p2) {}
    void N5332<T5332>(ref T5332 p1, DCon<T5332> p2) {}
    void N5333<T5333>(Con<T5333> p1, DInv<T5333> p2) {}
    void N5341<T5341>(T5341 p1, Cov<T5341> p2) {}
    void N5342<T5342>(ref T5342 p1, Con<T5342> p2) {}
    void N5343<T5343>(Con<T5343> p1, Inv<T5343> p2) {}
    void N541<T541>(Con<T541> p1, Class<T541> p2) {}

    void N61<T61>(ref T61 p1, Con<T61> p2) {}
    void N6211<T6211>(ref T6211 p1, Con<T6211[]> p2) {}
    void N6212<T6212>(Con<T6212> p1, Con<Struct<T6212>[]> p2) {}
    void N6231<T6231>(T6231 p1, Con<T6231[]> p2) {}
    void N6232<T6232>(Con<Struct<T6232>[]> p1) {}
    void N631<T631>(Con<T631> p1, Con<Class<T631>> p2) {}
    void N632<T632>(Con<T632> p1, Con<Struct<T632>> p2) {}
    void N6321<T6321>(Con<T6321> p1, Con<Struct<T6321>?> p2) {}
    void N6331<T6331>(ref T6331 p1, Con<DCov<T6331>> p2) {}
    void N6332<T6332>(T6332 p1, Con<DCon<T6332>> p2) {}
    void N6333<T6333>(Con<T6333> p1, Con<DInv<T6333>> p2) {}
    void N6341<T6341>(ref T6341 p1, Con<Cov<T6341>> p2) {}
    void N6342<T6342>(T6342 p1, Con<Con<T6342>> p2) {}
    void N6343<T6343>(Con<T6343> p1, Con<Inv<T6343>> p2) {}
    void N64<T64>(Con<T64> p1, Con<Derived<T64>> p2) {}
    void N6511<T6511>(ref T6511 p1, Con<Derived<T6511>> p2) {}
    void N6512<T6512>(T6512 p1, Con<Derived<T6512>> p2) {}
    void N6513<T6513>(Con<T6513> p1, Con<Derived<T6513>> p2) {}
    void N6531<T6531>(ref T6531 p1, Con<Interface<T6531>> p2) {}
    void N6532<T6532>(T6532 p1, Con<Interface<T6532>> p2) {}
    void N6533<T6533>(Con<T6533> p1, Con<Interface<T6533>> p2) {}

    void N71<A71, R71)(A71 p1, Func1<A71, R71> p2) {}
    Class<T71> M71<T71>(T71 p1) { return null; }

    Tiger tiger;
    Mammal mammal;
    Animal animal;

    Con<Tiger> conTiger;
    Inv<Tiger> invTiger;
    Cov<Tiger> covTiger;

    Con<Mammal> conMammal;
    Cov<Mammal> covMammal;
    Inv<Mammal> invMammal;

    Cov<Animal> covAnimal;
    Inv<Animal> invAnimal;
    Con<Animal> conAnimal;
    
    DCon<Mammal> dconMammal;
    DCov<Mammal> dcovMammal;
    DInv<Mammal> dinvMammal;
    
    Inv<Class<Mammal>> invClsMammal;
    Inv<Class<Mammal[]>> invClsMammalArr;

    Mammal[] mammalArr;
    Struct<Mammal>[] structMammalArr;
    Con<Mammal[]> conMammalArr;
    Con<Struct<Mammal>[]> conStructMammalArr;
    
    Class<Mammal> classMammal;
    Derived<Mammal> derivedMammal;
    Interface<Mammal> interfaceMammal;
    Struct<Mammal> structMammal;
    
    Con<Class<Mammal>> conClassMammal;
    Con<Struct<Mammal>> conStructMammal;
    Con<Struct<Mammal>?> conNullableStructMammal;
    Con<DCov<Mammal>> conDCovMammal;
    Con<DCon<Mammal>> conDConMammal;
    Con<DInv<Mammal>> conDInvMammal;
    Con<Cov<Mammal>> conCovMammal;
    Con<Con<Mammal>> conConMammal;
    Con<Inv<Mammal>> conInvMammal;

    Struct<Mammal>? nullableStructMammal;
    Class<Mammal>.Enum classMammalEnum;

    string str;
    

    void M() 
    { 
        // Method type inference test plan
        //
        // Method type inference is complicated and there are a lot of possibilities to test
        //
        // features not yet implemented are marked NYI
        // features not yet tested are marked NYT
        //
        // Strategies:
        //
        // When attempting to prove that an exact bound has been made, make an upper bound of Animal 
        // and an exact bound of Mammal. The parameter should be resolved as Mammal, even though Mammal is more specific.
        //
        // When attempting to prove that a lower bound has been made, make a lower bound of of Animal and 
        // a lower bound of Mammal. The parameter should be resolved as Animal; the Mammal bound is allowed to
        // relax and become Animal.
        //
        // When attempting to prove that an upper bound has been made, make an exact bound of Tiger and
        // an upper bound of Mammal. The parameter should be resolved as Tiger; the Mammal bound is allowed
        // to become Tiger.

        // 1 explicit lambda parameter to delegate parameter type (exact)

        N1((double x)=>123);                   //-C.N1<double>(FInt<double>)

        // 2 inferred lambda return type inference (NYI)
        //   2.1 to void delegate (NYI)
        //   2.2 to non-void delegate (NYI)

        // 3 method group return type inference
        //   3.1 to void delegate (NYT)
        //   3.2 to non void delegate 

        N32(M32);           //-C.N32<int>(Func0<int>)
        N32(str, M32);      //-C.N32<string, int>(string, Func1<string, int>)

        // 4 exact inference
        //   4.1 from any type S to method type parameter T (exact)
        //   4.2 from array S[] to array T[]
        //     4.2.1 where ranks match (exact)
        //     4.2.2 where ranks do not match (no inference made) (NYT)
        //   4.3 from constructed type 
        //     4.3.1 from class C<S> to class C<T> (exact)
        //     4.3.2 from struct C<S> to struct C<T> (exact)
        //       4.3.2.1 from nullable N<S> to N<T> (exact)
        //       4.3.2.2 from enum C<S>.E to enum C<T>.E (exact)
        //     4.3.3 from delegate C<S> to C<T>
        //       4.3.3.1 covariant (exact)
        //       4.3.3.2 contravariant (exact)
        //       4.3.3.3 invariant (exact)
        //     4.3.4 from interface C<S> to interface C<T>
        //       4.3.4.1 covariant (exact)
        //       4.3.4.2 contravariant (exact)
        //       4.3.4.3 invariant (exact)

        N41(conAnimal, ref mammal);                     //-C.N41<Mammal>(Con<Mammal>, ref Mammal)
        N42(conAnimal, ref mammalArr);                  //-C.N42<Mammal>(Con<Mammal>, ref Mammal[])
        N431(conAnimal, ref classMammal);               //-C.N431<Mammal>(Con<Mammal>, ref Class<Mammal>)
        N432(conAnimal, ref structMammal);              //-C.N432<Mammal>(Con<Mammal>, ref Struct<Mammal>)
        N4321(conAnimal, ref nullableStructMammal);     //-C.N4321<Mammal>(Con<Mammal>, ref Struct<Mammal>?)
        N4322(conAnimal, ref classMammalEnum);          //-C.N4322<Mammal>(Con<Mammal>, ref Class<Mammal>.Enum)
        N4331(conAnimal, ref dcovMammal);               //-C.N4331<Mammal>(Con<Mammal>, ref DCov<Mammal>)
        N4332(conAnimal, ref dconMammal);               //-C.N4332<Mammal>(Con<Mammal>, ref DCon<Mammal>)
        N4333(conAnimal, ref dinvMammal);               //-C.N4333<Mammal>(Con<Mammal>, ref DInv<Mammal>)
        N4341(conAnimal, ref covMammal);                //-C.N4341<Mammal>(Con<Mammal>, ref Cov<Mammal>)
        N4342(conAnimal, ref conMammal);                //-C.N4342<Mammal>(Con<Mammal>, ref Con<Mammal>)
        N4343(conAnimal, ref invMammal);                //-C.N4343<Mammal>(Con<Mammal>, ref Inv<Mammal>)

        // 5 lower bound inference
        //   5.1 from any type to method type parameter T (lower)
        //   5.2 from array S[]
        //     5.2.1 to array T[] of same rank
        //       5.2.1.1 of reference type (lower)
        //       5.2.1.2 of value type (exact)
        //     5.2.2 to array T[] of different rank (no inference made) (NYT)
        //     5.2.3 from 1-d array S[] to IEnumerable<T>, ICollection<T>, IList<T>, IReadOnlyList<T> and IReadOnlyCollection<T>
        //       5.2.3.1 of reference type (lower)
        //       5.2.3.2 of value type (exact)
        //   5.3 from constructed type to matching constructed type
        //     5.3.1 from class C<S> to C<T> (exact)
        //     5.3.2 from struct C<S> to C<T> (exact)
        //       5.3.2.1 nullable N<S> to N<T> (exact)
        //     5.3.3 from delegate C<S> to C<T>
        //       5.3.3.1 covariant (lower)
        //       5.3.3.2 contravariant (upper)
        //       5.3.3.3 invariant (exact)
        //     5.3.4 from interface C<S> to C<T>
        //       5.3.4.1 covariant (lower)
        //       5.3.4.2 contravariant (upper)
        //       5.3.4.3 invariant (exact)
        //   5.4 base class
        //     5.4.1 from class D with base class C<S> to C<T> (exact)
        //     5.4.2 from type parameter D with effective base class C<S> to C<T> (exact) (NYI)
        //     5.4.3 from type parameter D with effective base class E that has base class C<S> to C<T> (exact) (NYI)

        N51(animal, mammal);                    //-C.N51<Animal>(Animal, Animal)
        N5211(animal, mammalArr);               //-C.N5211<Animal>(Animal, Animal[])
        N5212(conAnimal, structMammalArr);      //-C.N5212<Mammal>(Con<Mammal>, Struct<Mammal>[])
        N5231A(animal, mammalArr);              //-C.N5231A<Animal>(Animal, System.Collections.Generic.IEnumerable<Animal>)
        N5231B(animal, mammalArr);              //-C.N5231B<Animal>(Animal, System.Collections.Generic.ICollection<Animal>)
        N5231C(animal, mammalArr);              //-C.N5231C<Animal>(Animal, System.Collections.Generic.IList<Animal>)
        N5231D(animal, mammalArr);              //-C.N5231D<Animal>(Animal, System.Collections.Generic.IReadOnlyCollection<Animal>)
        N5231E(animal, mammalArr);              //-C.N5231E<Animal>(Animal, System.Collections.Generic.IReadOnlyList<Animal>)
        N5232A(structMammalArr);                //-C.N5232A<Struct<Mammal>>(System.Collections.Generic.IEnumerable<Struct<Mammal>>)
        N5232B(structMammalArr);                //-C.N5232B<Struct<Mammal>>(System.Collections.Generic.ICollection<Struct<Mammal>>)
        N5232C(structMammalArr);                //-C.N5232C<Struct<Mammal>>(System.Collections.Generic.IList<Struct<Mammal>>)
        N5232D(structMammalArr);                //-C.N5232D<Struct<Mammal>>(System.Collections.Generic.IReadOnlyCollection<Struct<Mammal>>)
        N5232E(structMammalArr);                //-C.N5232E<Struct<Mammal>>(System.Collections.Generic.IReadOnlyList<Struct<Mammal>>)
        N5321(conAnimal, classMammal);          //-C.N5321<Mammal>(Con<Mammal>, Class<Mammal>)
        N5322(conAnimal, structMammal);         //-C.N5322<Mammal>(Con<Mammal>, Struct<Mammal>)
        N5323(conAnimal, nullableStructMammal); //-C.N5323<Mammal>(Con<Mammal>, Struct<Mammal>?)
        N5331(animal, dcovMammal);              //-C.N5331<Animal>(Animal, DCov<Animal>)
        N5332(ref tiger, dconMammal);           //-C.N5332<Tiger>(ref Tiger, DCon<Tiger>)
        N5333(conAnimal, dinvMammal);           //-C.N5333<Mammal>(Con<Mammal>, DInv<Mammal>)
        N5341(animal, covMammal);               //-C.N5341<Animal>(Animal, Cov<Animal>)
        N5342(ref tiger, conMammal);            //-C.N5342<Tiger>(ref Tiger, Con<Tiger>)
        N5343(conAnimal, invMammal);            //-C.N5343<Mammal>(Con<Mammal>, Inv<Mammal>)
        N541(conAnimal, derivedMammal);         //-C.N541<Mammal>(Con<Mammal>, Class<Mammal>)

        //   5.5 base interface 
        //     in all cases, base interface must be 'unique'. (NYT)
        //     5.5.1 from class D with base interface C<S> to C<T> 
        //       5.5.1.1 covariant (lower)
        //       5.5.1.2 contravariant (upper)
        //       5.5.1.3 invariant (exact)
        //     5.5.2 from struct D with base interface C<S> to C<T>
        //       5.5.2.1 covariant (lower)
        //       5.5.2.2 contravariant (upper)
        //       5.5.2.3 invariant (exact)
        //     5.5.3 from interface D with base interface C<S> to C<T>
        //       5.5.3.1 covariant (lower)
        //       5.5.3.2 contravariant (upper)
        //       5.5.3.3 invariant (exact)

        // Just re-use the methods from section 5.3.4

        N5341(animal, derivedMammal);               //-C.N5341<Animal>(Animal, Cov<Animal>)
        N5342(ref tiger, derivedMammal);            //-C.N5342<Tiger>(ref Tiger, Con<Tiger>)
        N5343(conAnimal, derivedMammal);            //-C.N5343<Mammal>(Con<Mammal>, Inv<Mammal>)
        N5341(animal, structMammal);                //-C.N5341<Animal>(Animal, Cov<Animal>)
        N5342(ref tiger, structMammal);             //-C.N5342<Tiger>(ref Tiger, Con<Tiger>)
        N5343(conAnimal, structMammal);             //-C.N5343<Mammal>(Con<Mammal>, Inv<Mammal>)
        N5341(animal, interfaceMammal);             //-C.N5341<Animal>(Animal, Cov<Animal>)
        N5342(ref tiger, interfaceMammal);          //-C.N5342<Tiger>(ref Tiger, Con<Tiger>)
        N5343(conAnimal, interfaceMammal);          //-C.N5343<Mammal>(Con<Mammal>, Inv<Mammal>)


        //     5.5.4 from type parameter D with effective base class E that implements interface C<S> to C<T> (NYI)
        //       5.5.4.1 covariant (lower)(NYI)
        //       5.5.4.2 contravariant (upper)(NYI)
        //       5.5.4.3 invariant (exact)(NYI)
        //     5.5.5 from type parameter D with effective interface set member C<S> to C<T> (NYI)
        //       5.5.5.1 covariant (lower)(NYI)
        //       5.5.5.2 contravariant (upper)(NYI)
        //       5.5.5.3 invariant (exact)(NYI)
        //     5.5.6 from type parameter D with effective interface set member E that inherits from C<S> to C<T> (NYI)
        //       5.5.6.1 covariant (lower)(NYI)
        //       5.5.6.2 contravariant (upper)(NYI)
        //       5.5.6.3 invariant (exact)(NYI)


        // 6 upper bound inference
        //   6.1 from any type S to method type parameter T (upper)
        //   6.2 to array T[]
        //     6.2.1 from array S[] of same rank
        //       6.2.1.1 of reference type (upper)
        //       6.2.1.2 of value type (exact)
        //     6.2.2 from array S[] of different rank (no inference made) (NYT)
        //     6.2.3 from IEnumerable<S>, ICollection<S> or IList<S> to one-d array T[] 
        //       6.2.3.1 of reference type (upper) 
        //       6.2.3.2 of value type (exact)
        //   6.3 from constructed type to matching constructed type
        //     6.3.1 from class C<S> to C<T> (exact)
        //     6.3.2 from struct C<S> to C<T> (exact)
        //       6.3.2.1 nullable N<S> to N<T> (exact)
        //     6.3.3 from delegate C<S> to C<T>
        //       6.3.3.1 covariant (upper)
        //       6.3.3.2 contravariant (lower)
        //       6.3.3.3 invariant (exact)
        //     6.3.4 from interface C<S> to C<T>
        //       6.3.4.1 covariant (upper)
        //       6.3.4.2 contravariant (lower)
        //       6.3.4.3 invariant (exact)
        //   6.4 from class C<S> to class D with base class C<T> (exact)
        //   6.5 base interface - in all cases, base interface must be 'unique'.
        //     6.5.1 from interface C<S> to class D with base interface C<T> 
        //       6.5.1.1 covariant (upper)
        //       6.5.1.2 contravariant (lower)
        //       6.5.1.3 invariant (exact)
        //     6.5.2 from interface C<S> to struct D with base interface C<T> (NYT)
        //       These are unusual cases because here method type inference will succeed but will
        //       always produce a result that is not applicable. If you have an upper bound inference
        //       because you are inferring from Con<Struct<S>> to Con<I<T>> where Struct<S> implements I<S>
        //       then we make the upper bound inference from I<S> to I<T>. If that succeeds then applicability
        //       checking will fail, because Con<any value type> is not convertible to Con<any reference type>.
        //       In order to verify that type inference is succeeding here we'll have to make some error cases
        //       and see if we're getting 'not applicable' errors vs 'type inference failed' errors.
        //       6.5.2.1 covariant (upper)(NYT)
        //       6.5.2.2 contravariant (lower)(NYT)
        //       6.5.2.3 invariant (exact)(NYT)
        //     6.5.3 from interface C<S> to interface D with base interface C<T>
        //       6.5.3.1 covariant (upper)
        //       6.5.3.2 contravariant (lower)
        //       6.5.3.3 invariant (exact)

        N61(ref tiger, conMammal);                  //-C.N61<Tiger>(ref Tiger, Con<Tiger>)
        N6211(ref tiger, conMammalArr);             //-C.N6211<Tiger>(ref Tiger, Con<Tiger[]>)
        N6212(conAnimal, conStructMammalArr);       //-C.N6212<Mammal>(Con<Mammal>, Con<Struct<Mammal>[]>)
        N6231(tiger, (Con<IEnumerable<Mammal>>)null);               //-C.N6231<Mammal>(Mammal, Con<Mammal[]>)
        N6231(tiger, (Con<IList<Mammal>>)null);                     //-C.N6231<Mammal>(Mammal, Con<Mammal[]>)
        N6231(tiger, (Con<IReadOnlyList<Mammal>>)null);             //-C.N6231<Mammal>(Mammal, Con<Mammal[]>)
        N6231(tiger, (Con<IReadOnlyCollection<Mammal>>)null);       //-C.N6231<Mammal>(Mammal, Con<Mammal[]>)
        N6232((Con<IEnumerable<Struct<Mammal>>>)null);              //-C.N6232<Mammal>(Con<Struct<Mammal>[]>)
        N6232((Con<IList<Struct<Mammal>>>)null);                    //-C.N6232<Mammal>(Con<Struct<Mammal>[]>)
        N6232((Con<IReadOnlyList<Struct<Mammal>>>)null);            //-C.N6232<Mammal>(Con<Struct<Mammal>[]>)
        N6232((Con<IReadOnlyCollection<Struct<Mammal>>>)null);      //-C.N6232<Mammal>(Con<Struct<Mammal>[]>)
        N631(conAnimal, conClassMammal);            //-C.N631<Mammal>(Con<Mammal>, Con<Class<Mammal>>)
        N632(conAnimal, conStructMammal);           //-C.N632<Mammal>(Con<Mammal>, Con<Struct<Mammal>>)
        N6321(conAnimal, conNullableStructMammal);  //-C.N6321<Mammal>(Con<Mammal>, Con<Struct<Mammal>?>)
        N6331(ref tiger, conDCovMammal);            //-C.N6331<Tiger>(ref Tiger, Con<DCov<Tiger>>)
        N6332(animal, conDConMammal);               //-C.N6332<Animal>(Animal, Con<DCon<Animal>>)
        N6333(conAnimal, conDInvMammal);            //-C.N6333<Mammal>(Con<Mammal>, Con<DInv<Mammal>>)
        N6341(ref tiger, conCovMammal);             //-C.N6341<Tiger>(ref Tiger, Con<Cov<Tiger>>)
        N6342(animal, conConMammal);                //-C.N6342<Animal>(Animal, Con<Con<Animal>>)
        N6343(conAnimal, conInvMammal);             //-C.N6343<Mammal>(Con<Mammal>, Con<Inv<Mammal>>)
        N64(conAnimal, conClassMammal);             //-C.N64<Mammal>(Con<Mammal>, Con<Derived<Mammal>>)
        N6511(ref tiger, conCovMammal);             //-C.N6511<Tiger>(ref Tiger, Con<Derived<Tiger>>)
        N6512(animal, conConMammal);                //-C.N6512<Animal>(Animal, Con<Derived<Animal>>)
        N6513(conAnimal, conInvMammal);             //-C.N6513<Mammal>(Con<Mammal>, Con<Derived<Mammal>>)
        N6531(ref tiger, conCovMammal);             //-C.N6531<Tiger>(ref Tiger, Con<Interface<Tiger>>)
        N6532(animal, conConMammal);                //-C.N6532<Animal>(Animal, Con<Interface<Animal>>)
        N6533(conAnimal, conInvMammal);             //-C.N6533<Mammal>(Con<Mammal>, Con<Interface<Mammal>>)

        // 7 Additional interesting scenarios:
        // 7.1 Method type inference where the argument is a method group containing a generic method that also
        //     needs method type inference:

        N71(1, M71); //-C.N71<int, Class<int>>(int, Func1<int, Class<int>>)
        
    }
}
