package org.mustbe.consulo.csharp.ide.refactoring.introduceVariable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import consulo.dotnet.psi.DotNetExpression;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.EditorComboBoxEditor;
import com.intellij.ui.EditorComboBoxRenderer;
import com.intellij.ui.StringComboboxEditor;

/**
 * User: Fedor.Korotkov
 */
public class CSharpIntroduceDialog extends DialogWrapper {
  private JPanel myContentPane;
  private JLabel myNameLabel;
  private ComboBox myNameComboBox;
  private JCheckBox myReplaceAll;

  private final Project myProject;
  private final int myOccurrencesCount;
  private final DotNetExpression myExpression;

  public CSharpIntroduceDialog(@NotNull final Project project, @NotNull final String caption, final CSharpIntroduceOperation operation) {
    super(project, true);
    myOccurrencesCount = operation.getOccurrences().size();
    myProject = project;
    myExpression = operation.getInitializer();
    setUpNameComboBox(operation.getSuggestedNames());

    setTitle(caption);
    init();
    setupDialog();
  }

  private void setUpNameComboBox(Collection<String> possibleNames) {
    final EditorComboBoxEditor comboEditor = new StringComboboxEditor(myProject, CSharpFileType.INSTANCE, myNameComboBox);

    myNameComboBox.setEditor(comboEditor);
    myNameComboBox.setRenderer(new EditorComboBoxRenderer(comboEditor));
    myNameComboBox.setEditable(true);
    myNameComboBox.setMaximumRowCount(8);

    myContentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myNameComboBox.requestFocus();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

    for (String possibleName : possibleNames) {
      myNameComboBox.addItem(possibleName);
    }
  }

  private void setupDialog() {
    myReplaceAll.setMnemonic(KeyEvent.VK_A);
    myNameLabel.setLabelFor(myNameComboBox);

    // Replace occurrences check box setup
    if (myOccurrencesCount > 1) {
      myReplaceAll.setSelected(false);
      myReplaceAll.setEnabled(true);
      myReplaceAll.setText(myReplaceAll.getText() + " (" + myOccurrencesCount + " occurrences)");
    }
    else {
      myReplaceAll.setSelected(false);
      myReplaceAll.setEnabled(false);
    }
  }

  public JComponent getPreferredFocusedComponent() {
    return myNameComboBox;
  }

  protected JComponent createCenterPanel() {
    return myContentPane;
  }

  @Nullable
  public String getName() {
    final Object item = myNameComboBox.getEditor().getItem();
    if ((item instanceof String) && ((String)item).length() > 0) {
      return ((String)item).trim();
    }
    return null;
  }

  public Project getProject() {
    return myProject;
  }

  public DotNetExpression getExpression() {
    return myExpression;
  }

  public boolean doReplaceAllOccurrences() {
    return myReplaceAll.isSelected();
  }
}
