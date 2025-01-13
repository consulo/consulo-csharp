using System;

// no errors
[assembly:SomeClassAA()]
[module:SomeClassAA()]

[SomeAttt()]
public class AssemblyAttributeBeforeAndAfterMember
{
}

// will report error
[assembly:SomeClassAA()]
[module:SomeClassAA()]
