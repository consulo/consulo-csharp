package org.musbe.consulo.csharp.formatting;


import consulo.testFramework.FormattingTestCase;

/**
 * @author VISTALL
 * @since 19.04.2015
 */
public class CSharpFormattingTest extends FormattingTestCase
{
	public CSharpFormattingTest()
	{
		super("/csharp-impl/testData/formatting/", "cs");
	}

	@Override
	protected boolean shouldContainTempFiles()
	{
		return false;
	}
	public void testIssue270()
	{
	}

	public void testIssue291()
	{
	}
}
