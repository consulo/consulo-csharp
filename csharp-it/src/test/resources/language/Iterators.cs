using System;
using System.Collections.Generic;

class C
{
    IEnumerable<int> A(ref int /*error:CS1623*/x) { yield return 1; }
    IEnumerable<int> B(out int /*error:CS1623*/x) { x = 0; yield return 1; }
    IEnumerable<int> In(in int /*error:CS1623*/x) { yield return 1; }
    int /*error:CS1624*/D() { yield return 1; }
    List<int> /*error:CS1624*/L() { yield return 1; }
    IEnumerable<int> E()
    {
        try { /*error:CS1626*/yield return 1; }
        catch { /*error:CS1631*/yield return 2; }
        finally { /*error:CS1625*/yield return 3; }
    }
    IEnumerable<int> F()
    {
        try { yield break; }
        catch { }
        try { yield return 1; }
        finally { }
        try { }
        catch { yield break; }
        try { }
        finally
        {
            Action a = () => { return; };
            /*error:CS1625*/yield break;
        }
    }
    IEnumerable<int> G()
    {
        Action a = () => { /*error:CS1621*/yield break; };
        Action b = delegate { /*error:CS1621*/yield break; };
        yield return 0;
    }
    IEnumerable<int> Local()
    {
        IEnumerable<string> Names() { yield return "x"; }
        int /*error:CS1624*/Bad() { yield return 1; }
        foreach (var name in Names()) { }
        yield return Bad();
    }
    IEnumerator<string> H() { yield return "x"; }
    System.Collections.IEnumerable N() { yield return 1; }
}
