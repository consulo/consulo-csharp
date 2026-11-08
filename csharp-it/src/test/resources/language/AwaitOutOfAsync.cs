// sdk:NET_10
using System;
using System.Threading.Tasks;

class AwaitOutOfAsync
{
    int Property
    {
        get
        {
            /*error:CS4032*/await Task.Yield();
            return 1;
        }
    }

    AwaitOutOfAsync()
    {
        /*error:CS4033*/await Task.Yield();
    }

    string Text()
    {
        /*error:CS4032*/await Task.Yield();
        return "";
    }

    void Void()
    {
        /*error:CS4033*/await Get();
        /*error:CS4033*/await this.Get();
        int Local()
        {
            /*error:CS4032*/await Task.Yield();
            return 1;
        }
        Action anonymous = delegate { /*error:CS4034*/await Task.Yield(); };
        Func<int> lambda = () => /*error:CS4034*/await Task.FromResult(1);
        Use(Local());
    }

    async Task Async()
    {
        await Task.Yield();
        Func<Task> lambda = async () => await Task.Yield();
        lock (this)
        {
            /*error:CS1996*/await Task.Yield();
        }
        await lambda();
    }

    void Name()
    {
        var await = 1;
        int other = await;
        await = 2;
        Use(other + await);
    }

    Task Get() => Task.CompletedTask;

    void Use(int value) { }
}
