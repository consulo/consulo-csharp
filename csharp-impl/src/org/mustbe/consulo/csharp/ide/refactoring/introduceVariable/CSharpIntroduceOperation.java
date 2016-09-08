package org.mustbe.consulo.csharp.ide.refactoring.introduceVariable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import consulo.dotnet.psi.DotNetExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

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
