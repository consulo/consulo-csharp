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

package consulo.csharp.impl.ide.assemblyInfo;

import consulo.application.ApplicationManager;
import consulo.fileEditor.FileEditorLocation;
import consulo.fileEditor.FileEditorState;
import consulo.fileEditor.FileEditorStateLevel;
import consulo.application.util.function.Computable;
import consulo.language.psi.PsiFile;
import consulo.ui.ex.awt.tree.SimpleTree;
import consulo.virtualFileSystem.ReadonlyStatusHandler;
import consulo.virtualFileSystem.VirtualFile;
import consulo.language.psi.PsiManager;
import consulo.ui.ex.awt.tree.ColoredTreeCellRenderer;
import consulo.ui.ex.awt.OnePixelSplitter;
import consulo.ui.ex.awt.ScrollPaneFactory;
import consulo.util.collection.ContainerUtil;
import consulo.ui.ex.awt.tree.TreeUtil;
import consulo.csharp.impl.ide.assemblyInfo.blocks.CSharpAssemblyBlock;
import consulo.csharp.impl.ide.assemblyInfo.blocks.CSharpSimpleStringAssemblyBlock;
import consulo.dotnet.DotNetTypes;
import consulo.fileEditor.FileEditor;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.dataholder.UserDataHolderBase;
import jakarta.annotation.Nonnull;
import kava.beans.PropertyChangeListener;

import jakarta.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

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
		myIsReadonlyFile = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> !ReadonlyStatusHandler.ensureFilesWritable(project, myVirtualFile));

		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Name", DotNetTypes.System.Reflection.AssemblyProductAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Title", DotNetTypes.System.Reflection.AssemblyTitleAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Description", DotNetTypes.System.Reflection.AssemblyDescriptionAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Company", DotNetTypes.System.Reflection.AssemblyDescriptionAttribute));

		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Copyright", DotNetTypes.System.Reflection.AssemblyCopyrightAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Trademark", DotNetTypes.System.Reflection.AssemblyTrademarkAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("Version", DotNetTypes.System.Reflection.AssemblyVersionAttribute));
		myBlocks.add(new CSharpSimpleStringAssemblyBlock("File Version", DotNetTypes.System.Reflection.AssemblyFileVersionAttribute));
	}

	@Nonnull
	@Override
	public JComponent getComponent()
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();

		for(CSharpAssemblyBlock block : myBlocks)
		{
			DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(block);

			root.add(newChild);
		}

		final SimpleTree simpleTree = new SimpleTree(new DefaultTreeModel(root));
		simpleTree.setRootVisible(false);
		simpleTree.setCellRenderer(new ColoredTreeCellRenderer()
		{
			@RequiredUIAccess
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

		final OnePixelSplitter splitter = new OnePixelSplitter();
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

	@Nonnull
	@Override
	public String getName()
	{
		return "Assembly Info Editor";
	}

	@Nonnull
	@Override
	public FileEditorState getState(@Nonnull FileEditorStateLevel fileEditorStateLevel)
	{
		return FileEditorState.INSTANCE;
	}

	@Override
	public void setState(@Nonnull FileEditorState fileEditorState)
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
	public void addPropertyChangeListener(@Nonnull PropertyChangeListener propertyChangeListener)
	{

	}

	@Override
	public void removePropertyChangeListener(@Nonnull PropertyChangeListener propertyChangeListener)
	{

	}

	@Nullable
	@Override
	public FileEditorLocation getCurrentLocation()
	{
		return null;
	}

	@Nullable
	@Override
	public VirtualFile getFile()
	{
		return myVirtualFile;
	}

	@Override
	public void dispose()
	{

	}
}
