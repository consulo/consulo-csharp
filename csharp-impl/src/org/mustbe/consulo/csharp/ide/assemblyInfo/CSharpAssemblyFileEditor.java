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
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.assemblyInfo.blocks.CSharpAssemblyBlock;
import org.mustbe.consulo.csharp.ide.assemblyInfo.blocks.CSharpSimpleStringAssemblyBlock;
import org.mustbe.consulo.dotnet.DotNetTypes;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.tree.TreeUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 09.03.14
 */
public class CSharpAssemblyFileEditor extends UserDataHolderBase implements FileEditor
{
	private final VirtualFile myVirtualFile;
	private final PsiFile myPsiFile;
	private final boolean myIsReadonlyFile;

	private List<CSharpAssemblyBlock> myBlocks = new ArrayList<CSharpAssemblyBlock>();

	public CSharpAssemblyFileEditor(final Project project, VirtualFile virtualFile)
	{
		myVirtualFile = virtualFile;
		myPsiFile = PsiManager.getInstance(project).findFile(virtualFile);
		assert myPsiFile != null;
		myIsReadonlyFile = ApplicationManager.getApplication().runReadAction(new Computable<Boolean>()
		{
			@Override
			public Boolean compute()
			{
				return !ReadonlyStatusHandler.ensureFilesWritable(project, myVirtualFile);
			}
		});

		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Name", DotNetTypes.System.Reflection.AssemblyProductAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Title", DotNetTypes.System.Reflection.AssemblyTitleAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Description", DotNetTypes.System.Reflection.AssemblyDescriptionAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Company", DotNetTypes.System.Reflection.AssemblyDescriptionAttribute));

		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Copyright", DotNetTypes.System.Reflection.AssemblyCopyrightAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Trademark", DotNetTypes.System.Reflection.AssemblyTrademarkAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Version", DotNetTypes.System.Reflection.AssemblyVersionAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("File Version", DotNetTypes.System.Reflection.AssemblyFileVersionAttribute));
	}

	@NotNull
	@Override
	public JComponent getComponent()
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();

		for(CSharpAssemblyBlock block : myBlocks)
		{
			DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(block);

			root.add(newChild);
		}

		val simpleTree = new SimpleTree(new DefaultTreeModel(root));
		simpleTree.setRootVisible(false);
		simpleTree.setCellRenderer(new ColoredTreeCellRenderer()
		{
			@Override
			public void customizeCellRenderer(JTree jTree, Object o, boolean b, boolean b2, boolean b3, int i, boolean b4)
			{
				Object object = ((DefaultMutableTreeNode) o).getUserObject();
				if(object instanceof CSharpAssemblyBlock)
				{
					append(((CSharpAssemblyBlock) object).getTitle());
				}
			}
		});

		val splitter = new OnePixelSplitter();
		simpleTree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				List<CSharpAssemblyBlock> cSharpAssemblyBlocks = TreeUtil.collectSelectedObjectsOfType(simpleTree, CSharpAssemblyBlock.class);

				CSharpAssemblyBlock firstItem = ContainerUtil.getFirstItem(cSharpAssemblyBlocks);
				if(firstItem == null)
				{
					splitter.setSecondComponent(new JPanel());
				}
				else
				{

					splitter.setSecondComponent(firstItem.createAndLoadComponent(myPsiFile, !myIsReadonlyFile));
				}
			}
		});

		splitter.setAndLoadSplitterProportionKey("CSharpAssemblyFileEditor");

		splitter.setFirstComponent(simpleTree);
		splitter.setSecondComponent(new JPanel());

		return ScrollPaneFactory.createScrollPane(splitter);
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
