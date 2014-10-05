package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import gnu.trove.THashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpTypeResolveContext;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import lombok.val;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public class CSharpTypeResolveContextImpl implements CSharpTypeResolveContext
{
	private static class Collector extends CSharpElementVisitor
	{
		private List<CSharpConstructorDeclaration> myDeConstructors;
		private List<CSharpConstructorDeclaration> myConstructors;
		private MultiMap<IElementType, CSharpMethodDeclaration> myOperatorsMap;
		private MultiMap<String, CSharpMethodDeclaration> myExtensionMap;
		private List<CSharpArrayMethodDeclaration> myIndexMethods;
		private THashMap<String, PsiElement> myBaseMap = new THashMap<String, PsiElement>();

		public Collector()
		{
		}

		@Override
		public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
		{
			if(declaration.isDeConstructor())
			{
				if(myDeConstructors == null)
				{
					myDeConstructors = new SmartList<CSharpConstructorDeclaration>();
				}
				myDeConstructors.add(declaration);
			}
			else
			{
				if(myConstructors == null)
				{
					myConstructors = new SmartList<CSharpConstructorDeclaration>();
				}
				myConstructors.add(declaration);
			}
		}

		@Override
		public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
		{
			if(declaration.isOperator())
			{
				IElementType operatorElementType = declaration.getOperatorElementType();
				if(operatorElementType == null)
				{
					return;
				}

				if(myOperatorsMap == null)
				{
					myOperatorsMap = new MultiMap<IElementType, CSharpMethodDeclaration>();
				}

				myOperatorsMap.putValue(operatorElementType, declaration);
			}
			else if(CSharpMethodImplUtil.isExtensionMethod(declaration))
			{
				String name = declaration.getName();
				if(name == null)
				{
					return;
				}

				if(myExtensionMap == null)
				{
					myExtensionMap = new MultiMap<String, CSharpMethodDeclaration>();
				}

				myExtensionMap.putValue(name, declaration);
			}
			else
			{
				// we dont interest in impl methods
				if(declaration.getTypeRefForImplement() != DotNetTypeRef.ERROR_TYPE)
				{
					return;
				}
				//TODO [VISTALL]
			}
		}

		@Override
		public void visitArrayMethodDeclaration(CSharpArrayMethodDeclaration declaration)
		{
			// we dont interest in impl methods
			if(declaration.getTypeRefForImplement() != DotNetTypeRef.ERROR_TYPE)
			{
				return;
			}

			if(myIndexMethods == null)
			{
				myIndexMethods = new SmartList<CSharpArrayMethodDeclaration>();
			}
			myIndexMethods.add(declaration);
		}

		@Override
		public void visitFieldDeclaration(CSharpFieldDeclaration declaration)
		{
			putIfAbsentAndNotNull(declaration.getName(), declaration, myBaseMap);
		}

		@Override
		public void visitEventDeclaration(CSharpEventDeclaration declaration)
		{
			putIfAbsentAndNotNull(declaration.getName(), declaration, myBaseMap);
		}

		@Override
		public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
		{
			putIfAbsentAndNotNull(declaration.getName(), declaration, myBaseMap);
		}

		@Override
		public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
		{
			putIfAbsentAndNotNull(declaration.getName(), declaration, myBaseMap);
		}
	}

	@Nullable
	private CSharpElementGroup myIndexMethodGroup;
	@Nullable
	private CSharpElementGroup myConstructorGroup;
	@Nullable
	private CSharpElementGroup myDeConstructorGroup;

	@Nullable
	private final Map<IElementType, CSharpElementGroup> myOperatorMap;
	private final Map<String, CSharpElementGroup> myExtensionMap;

	public CSharpTypeResolveContextImpl(CSharpTypeDeclaration typeDeclaration)
	{
		val project = typeDeclaration.getProject();

		Collector collector = new Collector();

		DotNetNamedElement[] members = typeDeclaration.getMembers();
		for(DotNetNamedElement member : members)
		{
			member.accept(collector);
		}

		myConstructorGroup = toGroup(project, collector.myConstructors);
		myDeConstructorGroup = toGroup(project, collector.myDeConstructors);
		myOperatorMap = convertToGroup(project, collector.myOperatorsMap);
		myExtensionMap = convertToGroup(project, collector.myExtensionMap);
	}

	@Nullable
	private static CSharpElementGroup toGroup(@NotNull Project project, @Nullable List<? extends PsiElement> elements)
	{
		if(ContainerUtil.isEmpty(elements))
		{
			return null;
		}
		return new CSharpElementGroupImpl(project, elements);
	}

	@Nullable
	private static <K, V extends PsiElement> Map<K, CSharpElementGroup> convertToGroup(@NotNull Project project, @Nullable MultiMap<K, V> multiMap)
	{
		if(multiMap == null || multiMap.isEmpty())
		{
			return null;
		}
		Map<K, CSharpElementGroup> map = new THashMap<K, CSharpElementGroup>(multiMap.size());
		for(Map.Entry<K, Collection<V>> entry : multiMap.entrySet())
		{
			map.put(entry.getKey(), new CSharpElementGroupImpl(project, entry.getValue()));
		}
		return map;
	}

	private static <K, V> void putIfAbsentAndNotNull(@Nullable K key, @NotNull V value, @NotNull Map<K, V> map)
	{
		if(key == null)
		{
			return;
		}
		if(!map.containsKey(key))
		{
			map.put(key, value);
		}
	}

	@Nullable
	@Override
	public CSharpElementGroup indexMethodGroup()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup constructorGroup()
	{
		return myConstructorGroup;
	}

	@Nullable
	@Override
	public CSharpElementGroup deConstructorGroup()
	{
		return myDeConstructorGroup;
	}

	@Nullable
	@Override
	public CSharpElementGroup findOperatorGroupByTokenType(@NotNull IElementType type)
	{
		if(myOperatorMap == null)
		{
			return null;
		}
		return myOperatorMap.get(type);
	}

	@Nullable
	@Override
	public CSharpElementGroup findExtensionMethodByName(@NotNull String name)
	{
		if(myExtensionMap == null)
		{
			return null;
		}
		return myExtensionMap.get(name);
	}

	@Override
	@Nullable
	public PsiElement findByName(@NotNull String name, boolean deep)
	{
		return null;
	}
}
