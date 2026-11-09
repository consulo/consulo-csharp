using System;

abstract class Base
{
    public virtual void Virtual() { }
    public void Plain() { }
    public int Field;
    public virtual int Property { get; set; }
    public virtual object Covariant() => null;
    public static void Static() { }
    public virtual void Overload(int value) { }
    public abstract void Abstract();
    public virtual int this[int index] => index;
    public virtual event Action Changed;
    private void Hidden() { }
}

class Derived : Base
{
    public override void /*error:CS0115*/Missing() { }
    public override void /*error:CS0506*/Plain() { }
    protected override void /*error:CS0507*/Virtual() { }
    public new void /*warning:CS0109*/Nothing() { }
    public static void /*warning:CS0108*/Static() { }
    public int /*warning:CS0108*/Field;
    public override string Covariant() => null;
    public override void /*error:CS0115*/Overload(string value) { }
    public new int Property { get; set; }
    public override void Abstract() { }
    public override int this[int index] => index + 1;
    public override event Action Changed;
    public void Hidden() { }
    public override string ToString() => "";
    public override int /*error:CS0115*/GetHashCode(int seed) => seed;
}

class Hiding : Base
{
    public void /*warning:CS0114*/Virtual() { }
    public int /*warning:CS0114*/Property { get; set; }
    public void Overload(string value) { }
    public override void Abstract() { }
    public new int Field;
}

class Generic<T>
{
    public virtual T Get(T value) => value;
    public virtual void Put(T value) { }
}

class Closed : Generic<string>
{
    public override string Get(string value) => value;
    public void /*warning:CS0114*/Put(string value) { }
}

class Deeper : Closed
{
    public override string Get(string value) => value + "!";
    public override void /*error:CS0115*/Put(int value) { }
}

interface IShape
{
    void Draw();
}

interface ICircle : IShape
{
    void Fill();
}
