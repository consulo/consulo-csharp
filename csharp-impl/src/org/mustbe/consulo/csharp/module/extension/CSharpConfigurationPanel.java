package org.mustbe.consulo.csharp.module.extension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;

import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.consulo.module.extension.ui.ModuleExtensionSdkBoxBuilder;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.csharp.compiler.CSharpCompilerBundleTypeProvider;
import org.mustbe.consulo.csharp.compiler.CSharpPlatform;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRendererWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.NullableFunction;

/**
 * @author VISTALL
 * @since 31.07.14
 */
public class CSharpConfigurationPanel extends JPanel
{
	private static final String[] ourCompileLevels = new String[]{
			"ISO-1",
			"ISO-2",
			"3",
			"4",
			"5",
			"6",
			"default"
	};

	@RequiredDispatchThread
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

					final CSharpModuleExtension extension = ModuleUtilCore.getExtension((Module) value,
							CSharpModuleExtension.class);
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
			// dont add self module
			if(module == ext.getModule())
			{
				continue;
			}

			final CSharpModuleExtension extension = (CSharpModuleExtension) ModuleUtilCore.getExtension(module,
					ext.getId());
			if(extension != null)
			{
				levelComboBox.addItem(module);
			}
		}

		final MutableModuleInheritableNamedPointer<CSharpLanguageVersion> languageVersionPointer = ext
				.getLanguageVersionPointer();
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

		final TextFieldWithHistory compileLevelField = new TextFieldWithHistory();
		compileLevelField.setHistory(Arrays.asList(ourCompileLevels));
		compileLevelField.setHistorySize(-1);
		String compilerTarget = ext.getCompilerTarget();
		if(compilerTarget != null)
		{
			compileLevelField.setText(compilerTarget);
		}
		compileLevelField.addDocumentListener(new DocumentAdapter()
		{
			@Override
			protected void textChanged(DocumentEvent e)
			{
				ext.setCompilerTarget(StringUtil.nullize(compileLevelField.getText(), true));
			}
		});

		add(new TitledSeparator("Compiler Options"));

		DotNetSimpleModuleExtension extension = ext.getModuleRootLayer().getExtension(DotNetSimpleModuleExtension
				.class);
		final Set<SdkType> compilerBundleTypes = new LinkedHashSet<SdkType>();
		if(extension != null)
		{
			for(CSharpCompilerBundleTypeProvider typeProvider : CSharpCompilerBundleTypeProvider.EP_NAME
					.getExtensions())
			{
				SdkType bundleType = typeProvider.getBundleType(extension);
				if(bundleType != null)
				{
					compilerBundleTypes.add(bundleType);
				}
			}
		}

		ModuleExtensionSdkBoxBuilder<CSharpMutableModuleExtension<?>> customCompilerBundleBoxBuilder =
				ModuleExtensionSdkBoxBuilder.<CSharpMutableModuleExtension<?>>create(ext, EmptyRunnable.getInstance());
		customCompilerBundleBoxBuilder.sdkTypes(compilerBundleTypes);
		customCompilerBundleBoxBuilder.sdkPointerFunc(new NullableFunction<CSharpMutableModuleExtension<?>,
				MutableModuleInheritableNamedPointer<Sdk>>()

		{
			@Nullable
			@Override
			public MutableModuleInheritableNamedPointer<Sdk> fun(CSharpMutableModuleExtension<?>
					cSharpMutableModuleExtension)
			{
				return ext.getCustomCompilerSdkPointer();
			}
		});
		customCompilerBundleBoxBuilder.nullItem("Auto Select", AllIcons.Actions.FindPlain);
		customCompilerBundleBoxBuilder.labelText("Compiler");

		add(customCompilerBundleBoxBuilder.build());

		add(LabeledComponent.left(compileLevelField, "Compiler Target (/langversion):"));

		final ComboBox platformComboBox = new ComboBox(CSharpPlatform.values());
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

		add(LabeledComponent.left(platformComboBox, "Platform (/platform): "));

		final JBCheckBox allowUnsafeCode = new JBCheckBox("Allow unsafe code (/unsafe)", ext.isAllowUnsafeCode());
		allowUnsafeCode.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ext.setAllowUnsafeCode(allowUnsafeCode.isSelected());
			}
		});

		add(allowUnsafeCode);

		final JBCheckBox optimizeCode = new JBCheckBox("Optimize Code (/optimize+)", ext.isOptimizeCode());
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
