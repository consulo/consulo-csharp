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

package consulo.csharp.lang.impl.evaluator;

import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.ast.IElementType;
import consulo.util.lang.Couple;
import consulo.util.lang.function.PairFunction;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 16.04.2015
 */
public class OperatorEvaluator
{
	private static Map<Couple<Class>, Map<IElementType, PairFunction<Object, Object, Object>>> ourOperators = new HashMap<Couple<Class>,
			Map<IElementType, PairFunction<Object, Object, Object>>>();

	static
	{
		// -----------------------------------  Integer Float -----------------------------------------------------

		register(Integer.class, Float.class, CSharpTokens.PLUS, new PairFunction<Integer, Float, Object>()
		{
			@Nullable
			@Override
			public Object fun(Integer t, Float v)
			{
				return t + v;
			}
		});

		register(Integer.class, Float.class, CSharpTokens.MINUS, new PairFunction<Integer, Float, Object>()
		{
			@Nullable
			@Override
			public Object fun(Integer t, Float v)
			{
				return t - v;
			}
		});

		register(Integer.class, Float.class, CSharpTokens.MUL, new PairFunction<Integer, Float, Object>()
		{
			@Nullable
			@Override
			public Object fun(Integer t, Float v)
			{
				return t * v;
			}
		});

		register(Integer.class, Float.class, CSharpTokens.DIV, new PairFunction<Integer, Float, Object>()
		{
			@Nullable
			@Override
			public Object fun(Integer t, Float v)
			{
				return t / v;
			}
		});

		// -----------------------------------  Float Integer -----------------------------------------------------

		register(Float.class, Integer.class, CSharpTokens.PLUS, new PairFunction<Float, Integer, Object>()
		{
			@Nullable
			@Override
			public Object fun(Float t, Integer v)
			{
				return t + v;
			}
		});

		register(Float.class, Integer.class, CSharpTokens.MINUS, new PairFunction<Float, Integer, Object>()
		{
			@Nullable
			@Override
			public Object fun(Float t, Integer v)
			{
				return t - v;
			}
		});

		register(Float.class, Integer.class, CSharpTokens.MUL, new PairFunction<Float, Integer, Object>()
		{
			@Nullable
			@Override
			public Object fun(Float t, Integer v)
			{
				return t * v;
			}
		});

		register(Float.class, Integer.class, CSharpTokens.DIV, new PairFunction<Float, Integer, Object>()
		{
			@Nullable
			@Override
			public Object fun(Float t, Integer v)
			{
				return t / v;
			}
		});

		// -----------------------------------  Float Float -----------------------------------------------------

		register(Float.class, Float.class, CSharpTokens.PLUS, new PairFunction<Float, Float, Object>()
		{
			@Nullable
			@Override
			public Object fun(Float t, Float v)
			{
				return t + v;
			}
		});

		register(Float.class, Float.class, CSharpTokens.MINUS, new PairFunction<Float, Float, Object>()
		{
			@Nullable
			@Override
			public Object fun(Float t, Float v)
			{
				return t - v;
			}
		});

		register(Float.class, Float.class, CSharpTokens.MUL, new PairFunction<Float, Float, Object>()
		{
			@Nullable
			@Override
			public Object fun(Float t, Float v)
			{
				return t * v;
			}
		});

		register(Float.class, Float.class, CSharpTokens.DIV, new PairFunction<Float, Float, Object>()
		{
			@Nullable
			@Override
			public Object fun(Float t, Float v)
			{
				return t / v;
			}
		});

		// -----------------------------------  Integer Integer -----------------------------------------------------

		register(Integer.class, Integer.class, CSharpTokens.PLUS, new PairFunction<Integer, Integer, Object>()
		{
			@Nullable
			@Override
			public Object fun(Integer t, Integer v)
			{
				return t + v;
			}
		});

		register(Integer.class, Integer.class, CSharpTokens.MINUS, new PairFunction<Integer, Integer, Object>()
		{
			@Nullable
			@Override
			public Object fun(Integer t, Integer v)
			{
				return t - v;
			}
		});

		register(Integer.class, Integer.class, CSharpTokens.MUL, new PairFunction<Integer, Integer, Object>()
		{
			@Nullable
			@Override
			public Object fun(Integer t, Integer v)
			{
				return t * v;
			}
		});

		register(Integer.class, Integer.class, CSharpTokens.DIV, new PairFunction<Integer, Integer, Object>()
		{
			@Nullable
			@Override
			public Object fun(Integer t, Integer v)
			{
				return t / v;
			}
		});
	}

	@SuppressWarnings("unchecked")
	private static <P1, P2> void register(Class<P1> p1Class, Class<P2> p2Class, IElementType elementType, PairFunction<P1, P2, Object> function)
	{
		Couple<Class> key = Couple.<Class>of(p1Class, p2Class);
		Map<IElementType, PairFunction<Object, Object, Object>> map = ourOperators.get(key);
		if(map == null)
		{
			ourOperators.put(key, map = new HashMap<IElementType, PairFunction<Object, Object, Object>>());
		}
		map.put(elementType, (PairFunction<Object, Object, Object>) function);
	}

	@Nullable
	public static Object calcBinary(IElementType elementType, Object p1, Object p2)
	{
		Couple<Class> key = Couple.<Class>of(p1.getClass(), p2.getClass());

		Map<IElementType, PairFunction<Object, Object, Object>> map = ourOperators.get(key);
		if(map == null)
		{
			return null;
		}

		PairFunction<Object, Object, Object> function = map.get(elementType);
		if(function == null)
		{
			return null;
		}
		return function.fun(p1, p2);
	}
}
