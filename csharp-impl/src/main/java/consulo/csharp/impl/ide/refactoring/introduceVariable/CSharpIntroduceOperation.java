/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.impl.ide.refactoring.introduceVariable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import consulo.codeEditor.Editor;
import consulo.dotnet.psi.DotNetExpression;
import consulo.project.Project;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;

/**
 * @author Fedor.Korotkov
 *
 * from google-dart
 */
public class CSharpIntroduceOperation
{
	private final Project myProject;
	private final Editor myEditor;
	private final PsiFile myFile;
	private String myName;
	private boolean myReplaceAll;
	private PsiElement myElement;
	private DotNetExpression myInitializer;
	private List<PsiElement> myOccurrences = Collections.emptyList();
	private Collection<String> mySuggestedNames;

	public CSharpIntroduceOperation(Project project, Editor editor, PsiFile file, String name)
	{
		myProject = project;
		myEditor = editor;
		myFile = file;
		myName = name;
	}

	public String getName()
	{
		return myName;
	}

	public void setName(@Nullable String name)
	{
		myName = name;
	}

	public Project getProject()
	{
		return myProject;
	}

	public Editor getEditor()
	{
		return myEditor;
	}

	public PsiFile getFile()
	{
		return myFile;
	}

	public PsiElement getElement()
	{
		return myElement;
	}

	public void setElement(PsiElement element)
	{
		myElement = element;
	}

	public boolean isReplaceAll()
	{
		return myReplaceAll;
	}

	public void setReplaceAll(boolean replaceAll)
	{
		myReplaceAll = replaceAll;
	}

	public DotNetExpression getInitializer()
	{
		return myInitializer;
	}

	public void setInitializer(DotNetExpression initializer)
	{
		myInitializer = initializer;
	}

	public List<PsiElement> getOccurrences()
	{
		return myOccurrences;
	}

	public void setOccurrences(List<PsiElement> occurrences)
	{
		myOccurrences = occurrences;
	}

	public Collection<String> getSuggestedNames()
	{
		return mySuggestedNames;
	}

	public void setSuggestedNames(Collection<String> suggestedNames)
	{
		mySuggestedNames = suggestedNames;
	}
}
