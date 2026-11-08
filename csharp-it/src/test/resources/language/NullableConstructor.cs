// sdk:NET_10
#nullable enable
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;

class Members
{
    string assigned;
    string? optional;
    int value;
    string initialized = "";
    public string Property { get; set; }
    public string WithInitializer { get; set; } = "";
    public string Computed => "";
    List<int> list;
    object helped;
    static string /*warning:CS8618*/shared;

    public /*warning:CS8618*/Members()
    {
        assigned = "";
        list = new List<int>();
        Init();
    }

    public Members(int x) : this()
    {
    }

    public /*warning:CS8618*/Members(bool condition)
    {
        if (condition)
        {
            assigned = "";
        }
        Property = "";
        list = new List<int>();
        helped = 1;
    }

    [MemberNotNull(nameof(helped))]
    void Init()
    {
        helped = 1;
    }
}

class NoConstructor
{
    string /*warning:CS8618*/name;
    string? optional;
}

class Generic<T>
{
    T /*warning:CS8618*/value;
}

class ValueGeneric<T> where T : struct
{
    T value;
}

class StaticConstructor
{
    static string name;
    static string other;

    static /*warning:CS8618*/StaticConstructor()
    {
        name = "";
    }
}

#nullable disable
class Disabled
{
    string name;

    public Disabled()
    {
    }
}
