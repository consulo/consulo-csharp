

interface IE<out COV> {}
delegate R F<out R> ();
delegate R F<in A, out R> (A a);
delegate R F<in A1, in A2, out R> (A1 a1, A2 a2);

class Customer { public string Name; public int ID; }
class Order { public decimal Total; public int? CustomerID; }

class L<T> { public void D<R>(F<T, R> f){} }

class C 
{ 
    IE<T> Where<T>(IE<T> s, F<T, bool> f) { return null; }
    IE<R> Select<A, R>(IE<A> s, F<A, R> f) { return null; }
    IE<R> Join<I, O, K, R>(IE<I> inner, IE<O> outer, F<I, K> innerKey, F<O, K> outerKey, F<I, O, R> selector) { return null; }
    T Apply<T>(F<T> f) { return f(); }
    T Apply<T>(F<T> f1, F<T> f2) { return f1(); }
    T Apply<T>(params F<T>[] fs) { return fs[0](); }
    static int GetID(Customer c) { return c.ID; }
 
    IE<Customer> customers;
    IE<Order> orders;
    
    void M() 
    { 
        object x1 = Where(customers, c=>c.Name == null);
        //-C.Where<Customer>(IE<Customer>, F<Customer, bool>)
        object x2 = Select(customers, c=>c.Name);
        //-C.Select<Customer, string>(IE<Customer>, F<Customer, string>)
        object x3 = Join(customers, orders, c=>c.ID, o=>o.CustomerID, (c,o)=>o.Total);
        //-C.Join<Customer, Order, int?, decimal>(IE<Customer>, IE<Order>, F<Customer, int?>, F<Order, int?>, F<Customer, Order, decimal>)
        object x4 = Select(customers, c=>{ if (c.Name == string.Empty) return null; else return c.Name; });
        //-C.Select<Customer, string>(IE<Customer>, F<Customer, string>)
        object x5 = Apply(()=>new Customer());
        //-C.Apply<Customer>(F<Customer>)
        object x6 = Apply(()=>new Customer(), ()=>{while(true){}});
        //-C.Apply<Customer>(F<Customer>, F<Customer>)
        object x7 = Apply(()=>1, ()=>2, ()=>{while(true){}});
        //-C.Apply<int>(params F<int>[])

        // Make sure that overload resolution and type inference involving method group conversions works:
        object x8 = Select(customers, GetID); 
        //-C.Select<Customer, int>(IE<Customer>, F<Customer, int>)

        (new L<int>()).D(x=>123.4);
        //-L<int>.D<double>(F<int, double>)

    }
}
