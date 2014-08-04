package org.mustbe.consulo.csharp.module.extension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JList;
import javax.swing.JPanel;

import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.ColoredListCellRendererWrapper;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBCheckBox;
import lombok.val;

/**
 * @author VISTALL
 * @since 31.07.14
 */
public class CSharpConfigurationPanel extends JPanel
{
	public CSharpConfigurationPanel(final CSharpMutableModuleExtension<?> ext)
	{
		super(new VerticalFlowLayout());
		final ComboBox levelComboBox = new ComboBox();
		levelComboBox.setRenderer(new ColoredListCellRendererWrapper<Object>()
		{
			@Override
			protected void doCustomize(JList list, Object value, int index, boolean selected, boolean hasFocus)
			{
				if(value instanceof CSharpLanguageVersion)
				{
					final CSharpLanguageVersion languageLevel = (CSharpLanguageVersion) value;
					append(languageLevel.getPresentableName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
					append(" ");
					append(languageLevel.getDescription(), SimpleTextAttributes.GRAY_ATTRIBUTES);
				}
				else if(value instanceof Module)
				{
					setIcon(AllIcons.Nodes.Module);
					append(((Module) value).getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);

					final CSharpModuleExtension extension = ModuleUtilCore.getExtension((Module) value, CSharpModuleExtension.class);
					if(extension != null)
					{
						final CSharpLanguageVersion languageLevel = extension.getLanguageVersion();
						append("(" + languageLevel.getPresentableName() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
					}
				}
				else if(value instanceof String)
				{
					setIcon(AllIcons.Nodes.Module);
					append((String) value, SimpleTextAttributes.ERROR_BOLD_ATTRIBUTES);
				}
			}
		});

		for(CSharpLanguageVersion languageLevel : CSharpLanguageVersion.values())
		{
			levelComboBox.addItem(languageLevel);
		}

		for(Module module : ModuleManager.getInstance(ext.getProject()).getModules())
		{
			// dont add self e
			if(module == ext.getModule())
			{
				continue;
			}

			final CSharpModuleExtension extension = (CSharpModuleExtension) ModuleUtilCore.getExtension(module, ext.getId());
			if(extension != null)
			{
				levelComboBox.addItem(module);
			}
		}

		final MutableModuleInheritableNamedPointer<CSharpLanguageVersion> languageVersionPointer = ext.getLanguageVersionPointer();
		final String moduleName = languageVersionPointer.getModuleName();
		if(moduleName != null)
		{
			final Module module = languageVersionPointer.getModule();
			if(module != null)
			{
				levelComboBox.setSelectedItem(module);
			}
			else
			{
				levelComboBox.addItem(moduleName);
			}
		}
		else
		{
			levelComboBox.setSelectedItem(languageVersionPointer.get());
		}

		levelComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				final Object selectedItem = levelComboBox.getSelectedItem();
				if(selectedItem instanceof Module)
				{
					languageVersionPointer.set(((Module) selectedItem).getName(), null);
				}
				else if(selectedItem instanceof CSharpLanguageVersion)
				{
					languageVersionPointer.set(null, ((CSharpLanguageVersion) selectedItem).getName());
				}
				else
				{
					languageVersionPointer.set(selectedItem.toString(), null);
				}
			}
		});

		add(LabeledComponent.left(levelComboBox, "Language Version: "));

		val platformComboBox = new ComboBox(CSharpPlatform.values());
		platformComboBox.setSelectedItem(ext.getPlatform());
		platformComboBox.setRenderer(new ColoredListCellRendererWrapper<CSharpPlatform>()
		{
			@Override
			protected void doCustomize(JList jList, CSharpPlatform cSharpPlatform, int i, boolean b, boolean b2)
			{
				switch(cSharpPlatform)
				{
					case ANY_CPU:
						append("Any CPU");
						break;
					case ANY_CPU_32BIT_PREFERRED:
						append("Any CPU");
						append(" (32 bit preferred)", SimpleTextAttributes.GRAY_ATTRIBUTES);
						break;
					case ARM:
						append("ARM");
						break;
					case X86:
						append("x86");
						break;
					case X64:
						append("x64");
						break;
					case ITANIUM:
						append("Itanium");
						break;
				}
			}
		});

		platformComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					ext.setPlatform((CSharpPlatform) platformComboBox.getSelectedItem());
				}
			}
		});

		add(LabeledComponent.left(platformComboBox, "Platform: "));

		val allowUnsafeCode = new JBCheckBox("Allow unsafe code?", ext.isAllowUnsafeCode());
		allowUnsafeCode.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ext.setAllowUnsafeCode(allowUnsafeCode.isSelected());
			}
		});

		add(allowUnsafeCode);

		val optimizeCode = new JBCheckBox("Optimize Code?", ext.isOptimizeCode());
		optimizeCode.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ext.setOptimizeCode(optimizeCode.isSelected());
			}
		});
		add(optimizeCode);
	}
}
