/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.lang.impl.psi;

import org.jspecify.annotations.Nullable;

/**
 * nint and nuint (C# 9): native integers - System.IntPtr and System.UIntPtr, the same types since C# 11. They are no keywords:
 * a declaration named so wins.
 *
 * @author VISTALL
 */
public final class CSharpNativeIntegerUtil {
    public static final String NINT = "nint";
    public static final String NUINT = "nuint";

    private CSharpNativeIntegerUtil() {
    }

    /**
     * @return the type of the name of a native integer, null for another name
     */
    @Nullable
    public static String toVmQName(@Nullable String name) {
        if (NINT.equals(name)) {
            return "System.IntPtr";
        }
        if (NUINT.equals(name)) {
            return "System.UIntPtr";
        }
        return null;
    }
}
