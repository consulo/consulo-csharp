// sdk:NET_10
using System;
using System.Collections;
using System.Threading.Tasks;

class ReturnAndAssign
{
    readonly int field;
    static readonly int shared;
    const int Constant = 1;
    int Computed => 1;

    void Use(int value) { }

    int Get() => 1;

    void VoidReturn()
    {
        /*error:CS0127*/return 1;
    }

    int ValueReturn()
    {
        /*error:CS0126*/return;
    }

    async Task AsyncTask()
    {
        await Task.Yield();
        /*error:CS1997*/return 1;
    }

    async Task<int> AsyncValue()
    {
        await Task.Yield();
        /*error:CS0126*/return;
    }

    IEnumerable Iterator()
    {
        yield return 1;
        /*error:CS1622*/return;
    }

    void Lambdas()
    {
        Action action = () => { /*error:CS8030*/return 1; };
        Func<int> function = () => { /*error:CS0126*/return; };
    }

    void BeforeDeclaration()
    {
        int y = /*unresolved*//*error:CS0841*/x;
        int x = 1;
        Use(y);
        Use(x);
    }

    void Readonly(ReturnAndAssign other)
    {
        /*error:CS0191*/field = 1;
        /*error:CS0198*/shared = 1;
        /*error:CS0191*/this.field = 2;
        /*error:CS0191*/other.field = 3;
        /*error:CS0191*/field++;
        Action action = () => /*error:CS0191*/field = 4;
    }

    static ReturnAndAssign()
    {
        shared = 1;
    }

    ReturnAndAssign()
    {
        field = 1;
        this.field = 2;
        field += 1;
    }

    void NotAssignable()
    {
        /*error:CS0131*/Constant = 2;
        /*error:CS0131*/Get() = 1;
    }
}
