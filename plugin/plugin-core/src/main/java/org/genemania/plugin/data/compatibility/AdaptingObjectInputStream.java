/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2008-2011 University of Toronto.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.genemania.plugin.data.compatibility;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * An ObjectInputStream that compensates for serialization formats through the
 * use of adapters.
 * 
 * For example, suppose you have an instance of class C which has been
 * serialized.  Later on, class C evolves into C' which retains most method
 * signatures as C.  However, we want to deserialize the instance as type C'.
 * This class produces a proxy that translates calls from C.m() to C'.m'()
 * for all m such that m' has the same signature as m.  Through the use of an
 * adapter, this proxy is translated into an actual instance of C'.
 */
public class AdaptingObjectInputStream extends ObjectInputStream {

	private AdapterStrategy strategy;
	
	protected AdaptingObjectInputStream(InputStream stream, AdapterStrategy strategy) throws IOException, SecurityException {
		super(stream);
		enableResolveObject(true);
		this.strategy = strategy;
	}
	
	@Override
	protected Object resolveObject(final Object obj) throws IOException {
		Class<? extends Object> type = obj.getClass();
		if (!strategy.createdThis(obj)) {
			return obj;
		}
		
		Adapter<?, ?> adapter = strategy.getAdapter(type.getName());
		if (adapter == null) {
			return obj;
		}
		
		try {
			return adapt(obj, adapter);
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	Object adapt(final Object object, Adapter<?, ?> adapter) throws ClassNotFoundException {
		Class<?> actualType = object.getClass();
		if (!strategy.shouldAdapt(actualType.getName())) {
			return object;
		}
		
		// Create a duck-typing proxy for object; i.e. as long as the proxy
		// and the proxied instance have the same method signatures, even
		// though they have different classes, it's still possible to
		// delegate calls to the proxied instance.
		
		// This proxy exists only to facilitate the process of implementing
		// an adapter.
		Object proxy = Proxy.newProxyInstance(adapter.getClass().getClassLoader(), new Class<?>[] { adapter.getInterface() }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
				String name = method.getName();
				Class<?>[] parameterTypes = method.getParameterTypes();
				Method targetMethod = object.getClass().getMethod(name, parameterTypes);
				
				// We may need elevated privileges to call public methods on
				// non-public classes.
				targetMethod.setAccessible(true);
				
				Object result = targetMethod.invoke(object, arguments);
				Class<? extends Object> resultType = result.getClass();
				
				// Check if we need to adapt the return value.
				if (!strategy.createdThis(result)) {
					return result;
				}
				return adapt(result, strategy.getAdapter(resultType.getName()));
			}
		});
		
		return ((Adapter<? super Object, ?>) adapter).adapt(proxy);
	}
	
	@Override
	protected Class<?> resolveClass(ObjectStreamClass classDescription) throws IOException, ClassNotFoundException {
		String name = classDescription.getName();
		if (!strategy.shouldAdapt(name)) {
			return Class.forName(name, false, ClassLoader.getSystemClassLoader());
		}
		return strategy.resolveClass(name);
	}
}
