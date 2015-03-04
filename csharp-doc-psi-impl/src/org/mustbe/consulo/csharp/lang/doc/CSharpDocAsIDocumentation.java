/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.doc;

import org.emonic.base.documentation.IDocumentation;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocRoot;

/**
 * @author VISTALL
 * @since 04.03.2015
 */
public class CSharpDocAsIDocumentation implements IDocumentation
{
	private final CSharpDocRoot myDocRoot;

	public CSharpDocAsIDocumentation(CSharpDocRoot docRoot)
	{
		myDocRoot = docRoot;
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public String getSummary()
	{
		return myDocRoot.getTagText("summary");
	}

	@Override
	public String getRemarks()
	{
		return myDocRoot.getTagText("remarks");
	}

	@Override
	public String getValue()
	{
		return myDocRoot.getTagText("value");
	}

	@Override
	public String getReturns()
	{
		return myDocRoot.getTagText("returns");
	}

	@Override
	public String getSeeAlso()
	{
		return myDocRoot.getTagText("see");
	}

	@Override
	public IDocumentation[] getParams()
	{
		return new IDocumentation[0];
	}

	@Override
	public IDocumentation[] getExceptions()
	{
		return new IDocumentation[0];
	}
}
