package org.mustbe.consulo.csharp.ide.completion.insertHandler;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;

/**
 * @author VISTALL
 * @since 20.04.2016
 */
public class CSharpTailInsertHandler implements InsertHandler<LookupElement>
{
	private final TailType myTailType;

	public CSharpTailInsertHandler(TailType tailType)
	{
		myTailType = tailType;
	}

	@Override
	public void handleInsert(InsertionContext context, LookupElement item)
	{
		if(myTailType.isApplicable(context))
		{
			myTailType.processTail(context.getEditor(), context.getTailOffset());
		}
	}
}
