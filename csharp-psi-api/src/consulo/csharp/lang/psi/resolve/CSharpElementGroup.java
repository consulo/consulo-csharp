package consulo.csharp.lang.psi.resolve;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public interface CSharpElementGroup<T extends PsiElement> extends PsiNamedElement
{
	@NotNull
	Collection<T> getElements();

	boolean process(@NotNull Processor<? super T> processor);

	@Override
	@NotNull
	String getName();

	@NotNull
	Object getKey();
}
