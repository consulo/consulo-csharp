package org.musbe.consulo.csharp.formatting;

import org.mustbe.consulo.testFramework.FormattingTestCase;

/**
 * @author VISTALL
 * @since 19.04.2015
 */
public class CSharpFormattingTest extends FormattingTestCase
{
	public CSharpFormattingTest()
	{
		super("formatting", "cs");
	}

	@Override
	protected boolean shouldContainTempFiles()
	{
		return false;
	}

	@Override
	protected String getTestDataPath()
	{
		return "/csharp-impl/testData";
	}

	public void testIssue270()
	{
	}
}
