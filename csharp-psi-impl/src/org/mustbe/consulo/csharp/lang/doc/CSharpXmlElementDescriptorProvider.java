package org.mustbe.consulo.csharp.lang.doc;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import com.intellij.xml.impl.schema.XmlElementDescriptorImpl;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public class CSharpXmlElementDescriptorProvider implements XmlElementDescriptorProvider
{
	@Nullable
	@Override
	public XmlElementDescriptor getDescriptor(XmlTag xmlTag)
	{
		PsiFile containingFile = xmlTag.getContainingFile();
		if(!(containingFile instanceof CSharpFileImpl))
		{
			return null;
		}

		if(xmlTag.getName().equals("summary"))
		{
			return new XmlElementDescriptorImpl(xmlTag)
			{
				@Override
				public XmlAttributeDescriptor[] getAttributesDescriptors(final XmlTag context)
				{
					return new XmlAttributeDescriptor[]{
							new AnyXmlAttributeDescriptor("cref")
							{
								@Override
								public PsiElement getDeclaration()
								{
									return context;
								}
							}
					};
				}
			};
		}
		return null;

	}
}
