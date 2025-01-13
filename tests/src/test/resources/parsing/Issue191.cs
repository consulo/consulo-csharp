using System;
using System.Collections.Generic;


public class TestTest {
    public static void Main() {
        Object o = "";

        String a = o is String? ? ""  : "nope"; // ok
        String a = o is String ? ""  : "nope";
    }

}