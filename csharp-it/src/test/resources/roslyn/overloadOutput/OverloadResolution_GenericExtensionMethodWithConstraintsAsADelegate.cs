// expectedOutput: RemoveDetail\nRemoveDetail\nRemoveDetail\nRemoveDetail
using System;

public interface IDetail<T>
{

}

public interface IMain<T>
{

}

public class MyClass
{
    static void Main()
    {
        Principal aPrincipal = new Principal();
        Test(aPrincipal.RemoveDetail);
        Test(aPrincipal.RemoveDetail<Principal,Permission>);

        Action<Permission> a;
        a = aPrincipal.RemoveDetail;
        a(null);
        a = aPrincipal.RemoveDetail<Principal,Permission>;
        a(null);
    }

    static void Test(Action<Permission> a)
    {
        a(null);
    }    
}

public class Permission : IDetail<Principal>
{

}

public class Principal : IMain<Permission>
{
}

public static class Class
{
    public static void RemoveDetail<TMain, TChild>(this TMain main, TChild child)
        where TMain : class, IMain<TChild>
        where TChild : class, IDetail<TMain>
    {
        System.Console.WriteLine("RemoveDetail");
    }
}