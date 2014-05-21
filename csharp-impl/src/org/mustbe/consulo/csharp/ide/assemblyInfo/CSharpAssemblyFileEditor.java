/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide.assemblyInfo;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.assemblyInfo.blocks.CSharpAssemblyBlock;
import org.mustbe.consulo.csharp.ide.assemblyInfo.blocks.CSharpSimpleStringAssemblyBlock;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBScrollPane;

/**
 * @author VISTALL
 * @since 09.03.14
 */
public class CSharpAssemblyFileEditor extends UserDataHolderBase implements FileEditor
{
	private List<CSharpAssemblyBlock> myFirstBlocks = new ArrayList<CSharpAssemblyBlock>();
	private List<CSharpAssemblyBlock> mySecondBlocks = new ArrayList<CSharpAssemblyBlock>();
	private List<CSharpAssemblyBlock> myThirdBlocks = new ArrayList<CSharpAssemblyBlock>();

	private final VirtualFile myVirtualFile;
	private final PsiFile myPsiFile;

	public CSharpAssemblyFileEditor(Project project, VirtualFile virtualFile)
	{
		myVirtualFile = virtualFile;
		myPsiFile = PsiManager.getInstance(project).findFile(virtualFile);
		assert myPsiFile != null;

		myFirstBlocks.add(new CSharpSimpleStringAssemblyBlock("Title", CSharpAssemblyConstants.AssemblyTitleAttribute));
		myFirstBlocks.add(new CSharpSimpleStringAssemblyBlock("Description", CSharpAssemblyConstants.AssemblyDescriptionAttribute));

		mySecondBlocks.add(new CSharpSimpleStringAssemblyBlock("Title", CSharpAssemblyConstants.AssemblyTitleAttribute));
		mySecondBlocks.add(new CSharpSimpleStringAssemblyBlock("Title", CSharpAssemblyConstants.AssemblyTitleAttribute));

		myThirdBlocks.add(new CSharpSimpleStringAssemblyBlock("Title", CSharpAssemblyConstants.AssemblyTitleAttribute));
		myThirdBlocks.add(new CSharpSimpleStringAssemblyBlock("Title", CSharpAssemblyConstants.AssemblyTitleAttribute));
	}

	@NotNull
	@Override
	public JComponent getComponent()
	{
		JPanel panel = new JPanel(new HorizontalLayout());

		JPanel fPanel = new JPanel(new VerticalFlowLayout());
		for(CSharpAssemblyBlock block : myFirstBlocks)
		{
			fPanel.add(block.createComponent());
		}
		panel.add(fPanel);

		JPanel sPanel = new JPanel(new VerticalFlowLayout());
		for(CSharpAssemblyBlock block : mySecondBlocks)
		{
			sPanel.add(block.createComponent());
		}
		panel.add(sPanel);

		JPanel tPanel = new JPanel(new VerticalFlowLayout());
		for(CSharpAssemblyBlock block : myThirdBlocks)
		{
			tPanel.add(block.createComponent());
		}
		panel.add(tPanel);

		return new JBScrollPane(panel);
	}

	@Nullable
	@Override
	public JComponent getPreferredFocusedComponent()
	{
		return null;
	}

	@NotNull
	@Override
	public String getName()
	{
		return "Assembly Info Editor";
	}

	@NotNull
	@Override
	public FileEditorState getState(@NotNull FileEditorStateLevel fileEditorStateLevel)
	{
		return FileEditorState.INSTANCE;
	}

	@Override
	public void setState(@NotNull FileEditorState fileEditorState)
	{

	}

	@Override
	public boolean isModified()
	{
		return false;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void selectNotify()
	{

	}

	@Override
	public void deselectNotify()
	{

	}

	@Override
	public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener)
	{

	}

	@Override
	public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener)
	{

	}

	@Nullable
	@Override
	public BackgroundEditorHighlighter getBackgroundHighlighter()
	{
		return null;
	}

	@Nullable
	@Override
	public FileEditorLocation getCurrentLocation()
	{
		return null;
	}

	@Nullable
	@Override
	public StructureViewBuilder getStructureViewBuilder()
	{
		return null;
	}

	@Nullable
	@Override
	public VirtualFile getVirtualFile()
	{
		return myVirtualFile;
	}

	@Override
	public void dispose()
	{

	}
}
