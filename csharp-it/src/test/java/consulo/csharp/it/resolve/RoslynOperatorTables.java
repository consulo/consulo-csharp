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

package consulo.csharp.it.resolve;

import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tables of Roslyn ({@code roslyn/BinaryOperatorEasyOut.cs}, {@code roslyn/UnaryOperatorEasyOut.cs},
 * {@code roslyn/ConversionEasyOut.cs}): for every pair of predefined types the operator C# picks and the conversion it
 * classifies, read straight from the C# source of the compiler.
 *
 * @author VISTALL
 */
public final class RoslynOperatorTables
{
	/**
	 * The order of the rows and the columns of every table.
	 */
	public static final List<String> TYPES = List.of("object", "string", "bool", "char", "sbyte", "short", "int", "long", "byte", "ushort", "uint",
			"ulong", "nint", "nuint", "float", "double", "decimal", "bool?", "char?", "sbyte?", "short?", "int?", "long?", "byte?", "ushort?", "uint?",
			"ulong?", "nint?", "nuint?", "float?", "double?", "decimal?");

	private static final Map<String, String> ourKindTypes = new HashMap<>();

	static
	{
		// binary kinds
		putKind("INT", "int");
		putKind("UIN", "uint");
		putKind("LNG", "long");
		putKind("ULG", "ulong");
		putKind("NIN", "nint");
		putKind("NUI", "nuint");
		putKind("FLT", "float");
		putKind("DBL", "double");
		putKind("DEC", "decimal");
		putKind("BOL", "bool");
		ourKindTypes.put("LIN", "int?");
		ourKindTypes.put("LUN", "uint?");
		ourKindTypes.put("LLG", "long?");
		ourKindTypes.put("LUL", "ulong?");
		ourKindTypes.put("LFL", "float?");
		ourKindTypes.put("LDB", "double?");
		ourKindTypes.put("LDC", "decimal?");
		ourKindTypes.put("LBL", "bool?");
		ourKindTypes.put("LNI", "nint?");
		ourKindTypes.put("LNU", "nuint?");
		// unary kinds
		putKind("CHR", "char");
		putKind("I08", "sbyte");
		putKind("U08", "byte");
		putKind("I16", "short");
		putKind("U16", "ushort");
		putKind("I32", "int");
		putKind("U32", "uint");
		putKind("I64", "long");
		putKind("U64", "ulong");
		putKind("R32", "float");
		putKind("R64", "double");
	}

	private static void putKind(String kind, String type)
	{
		ourKindTypes.put(kind, type);
		ourKindTypes.put("L" + kind, type + "?");
	}

	private final Map<String, String[][]> myBinaryTables;
	private final Map<String, String[]> myUnaryTables;
	private final String[][] myConversions;

	private RoslynOperatorTables(Map<String, String[][]> binaryTables, Map<String, String[]> unaryTables, String[][] conversions)
	{
		myBinaryTables = binaryTables;
		myUnaryTables = unaryTables;
		myConversions = conversions;
	}

	public static RoslynOperatorTables load() throws Exception
	{
		String binary = Files.readString(CSharpResolveTesting.resource("roslyn/BinaryOperatorEasyOut.cs"), StandardCharsets.UTF_8);
		String unary = Files.readString(CSharpResolveTesting.resource("roslyn/UnaryOperatorEasyOut.cs"), StandardCharsets.UTF_8);

		Map<String, String[][]> binaryTables = new HashMap<>();
		Matcher table = Pattern.compile("BinaryOperatorKind\\[,] (s_\\w+) =\\s*\\{(.*?)\\n\\s*};", Pattern.DOTALL).matcher(binary);
		while(table.find())
		{
			List<String[]> rows = new ArrayList<>();
			Matcher row = Pattern.compile("/\\*\\s*[\\w?]+\\s*\\*/\\s*\\{([^}]*)}").matcher(table.group(2));
			while(row.find())
			{
				rows.add(cells(row.group(1)));
			}
			assertThat(rows).as(table.group(1)).hasSize(TYPES.size());
			binaryTables.put(table.group(1), rows.toArray(String[][]::new));
		}

		Map<String, String[]> unaryTables = new HashMap<>();
		Matcher unaryTable = Pattern.compile("UnaryOperatorKind\\[] (s_\\w+) =\\s*(?://[^\\n]*\\n)?\\s*\\{(.*?)};", Pattern.DOTALL).matcher(unary);
		while(unaryTable.find())
		{
			String[] cells = cells(unaryTable.group(2).replaceAll("/\\*.*?\\*/", ""));
			assertThat(cells).as(unaryTable.group(1)).hasSize(TYPES.size());
			unaryTables.put(unaryTable.group(1), cells);
		}
		String conversion = Files.readString(CSharpResolveTesting.resource("roslyn/ConversionEasyOut.cs"), StandardCharsets.UTF_8);
		Matcher conversionTable = Pattern.compile("s_convkind = new byte\\[,] \\{(.*?)\\n\\s*};", Pattern.DOTALL).matcher(conversion);
		assertThat(conversionTable.find()).as("s_convkind").isTrue();
		List<String[]> conversionRows = new ArrayList<>();
		Matcher conversionRow = Pattern.compile("/\\*\\s*[\\w?]+\\s*\\*/\\s*\\{([^}]*)}").matcher(conversionTable.group(1));
		while(conversionRow.find())
		{
			conversionRows.add(cells(conversionRow.group(1)));
		}
		assertThat(conversionRows).as("s_convkind").hasSize(TYPES.size());

		return new RoslynOperatorTables(binaryTables, unaryTables, conversionRows.toArray(String[][]::new));
	}

	private static String[] cells(String text)
	{
		List<String> cells = new ArrayList<>();
		for(String cell : text.split(","))
		{
			String trimmed = cell.strip();
			if(!trimmed.isEmpty())
			{
				cells.add(trimmed);
			}
		}
		return cells.toArray(String[]::new);
	}

	/**
	 * @return the kind code of the operator (INT, LIN, OBJ, ERR...) for {@code left op right}
	 */
	public String binary(String table, String left, String right)
	{
		String[][] cells = myBinaryTables.get(table);
		assertThat(cells).as(table).isNotNull();
		return cells[TYPES.indexOf(left)][TYPES.indexOf(right)];
	}

	/**
	 * @return the conversion kind code (IDN, NUM, NUL, BOX, IRF - implicit; XNM, XNL, XRF, UNB - explicit; NOC) from
	 * {@code source} to {@code target}
	 */
	public String conversion(String source, String target)
	{
		return myConversions[TYPES.indexOf(source)][TYPES.indexOf(target)];
	}

	public String unary(String table, String operand)
	{
		String[] cells = myUnaryTables.get(table);
		assertThat(cells).as(table).isNotNull();
		return cells[TYPES.indexOf(operand)];
	}

	/**
	 * @return the C# type of the operands of an operator of this kind, or null for ERR - and for OBJ, STR, OSC, SOC,
	 * whose operands are not of one type
	 */
	@Nullable
	public static String kindType(String kind)
	{
		return ourKindTypes.get(kind);
	}
}
