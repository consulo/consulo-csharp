/*
 * Copyright 2013-2026 consulo.io
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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * C# 9 top-level statements - the statements of a file out of any type: the body of an implicit
 * {@code static void <Main>$(string[] args)} of an implicit {@code Program} class (Roslyn SynthesizedSimpleProgramEntryPointSymbol).
 * Its locals are visible to the next statements, its local functions and {@code args} to all of them.
 *
 * @author VISTALL
 */
public class CSharpTopLevelStatementsImpl extends CSharpBlockStatementImpl implements CSharpSimpleLikeMethodAsElement {
    public static final String IMPLICIT_CLASS_NAME = "Program";
    public static final String IMPLICIT_METHOD_NAME = "<Main>$";
    public static final String ARGS_NAME = "args";

    public CSharpTopLevelStatementsImpl(IElementType elementType) {
        super(elementType);
    }

    @RequiredReadAction
    public CSharpLightParameterBuilder getArgsParameter() {
        return new CSharpLightParameterBuilder(this).withName(ARGS_NAME).withTypeRef(argsTypeRef());
    }

    @RequiredReadAction
    private DotNetTypeRef argsTypeRef() {
        return new CSharpArrayTypeRef(getProject(), getResolveScope(), new CSharpTypeRefByQName(this, DotNetTypes.System.String), 0);
    }

    @Override
    @RequiredReadAction
    public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, PsiElement lastParent, PsiElement place) {
        if (!super.processDeclarations(processor, state, lastParent, place)) {
            return false;
        }
        return processor.execute(getArgsParameter(), state);
    }

    @RequiredReadAction
    @Override
    public CSharpSimpleParameterInfo[] getParameterInfos() {
        return new CSharpSimpleParameterInfo[]{new CSharpSimpleParameterInfo(0, ARGS_NAME, this, argsTypeRef())};
    }

    /**
     * {@code int} with a {@code return value;}, {@code Task} / {@code Task<int>} with an {@code await} - else {@code void}
     */
    @RequiredReadAction
    @Override
    public DotNetTypeRef getReturnTypeRef() {
        boolean returnsValue = hasOwn(CSharpReturnStatementImpl.class, it -> it.getExpression() != null);
        DotNetTypeRef result = new CSharpTypeRefByQName(this, returnsValue ? DotNetTypes.System.Int32 : DotNetTypes.System.Void);
        if (!isAsync()) {
            return result;
        }
        if (!returnsValue) {
            return new CSharpTypeRefByQName(this, DotNetTypes2.System.Threading.Tasks.Task);
        }
        return new CSharpGenericWrapperTypeRef(getProject(), getResolveScope(), new CSharpTypeRefByQName(this, DotNetTypes2.System.Threading.Tasks.Task$1), result);
    }

    @RequiredReadAction
    private boolean isAsync() {
        return hasOwn(CSharpAwaitExpressionImpl.class, it -> true);
    }

    /**
     * An element of these statements - not of a lambda, an anonymous method or a local function in them
     */
    @RequiredReadAction
    private <T extends PsiElement> boolean hasOwn(Class<T> clazz, java.util.function.Predicate<T> condition) {
        for (T element : PsiTreeUtil.findChildrenOfType(this, clazz)) {
            if (PsiTreeUtil.getParentOfType(element, CSharpSimpleLikeMethodAsElement.class) == this && condition.test(element)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    @Override
    public boolean hasModifier(DotNetModifier modifier) {
        CSharpModifier cSharpModifier = CSharpModifier.as(modifier);
        return cSharpModifier == CSharpModifier.STATIC || cSharpModifier == CSharpModifier.ASYNC && isAsync();
    }

    @RequiredReadAction
    @Nullable
    @Override
    public DotNetModifierList getModifierList() {
        return null;
    }
}
