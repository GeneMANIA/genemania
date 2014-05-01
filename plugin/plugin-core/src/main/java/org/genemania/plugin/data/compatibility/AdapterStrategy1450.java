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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import no.uib.cipr.matrix.DenseMatrix;

/**
 * This class adapts instances from MTJ version 0.9.9e to the corresponding
 * instances in version 0.9.14b.  See ticket #1450 in Trac.
 */
public class AdapterStrategy1450 implements AdapterStrategy {
	private static final Map<String, Adapter<?, ?>> adapters = new HashMap<String, Adapter<?, ?>>();
	
	static {
		adapters.put("no.uib.cipr.matrix.DenseMatrix", //$NON-NLS-1$
				     new Adapter<MatrixProxy, DenseMatrix>() {
			@Override
			public Class<MatrixProxy> getInterface() {
				return MatrixProxy.class;
			}
			
			@Override
			public DenseMatrix adapt(MatrixProxy object) {
				int rows = object.numRows();
				int columns = object.numColumns();
				DenseMatrix result = new DenseMatrix(rows, columns);
				double[] data = object.getData();
				
				try {
					// Hack: Use reflection to set AbstractDenseMatrix.data.
					Class<?> type = Class.forName("no.uib.cipr.matrix.AbstractDenseMatrix"); //$NON-NLS-1$
					Field field = type.getDeclaredField("data"); //$NON-NLS-1$
					
					// Pro-tip: When dealing with a non-public field, make it
					// accessible and hope the SecurityManager doesn't take
					// offense.
					field.setAccessible(true);
					field.set(result, data);
				} catch (SecurityException e) {
					throw new RuntimeException(e);
				} catch (NoSuchFieldException e) {
					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
				return result;
			}
		});
	}

	private ClassLoader classLoader;

	/**
	 * Creates a new instance using the given ClassLoader, which can
	 * resolve classes from MTJ 0.9.9e.
	 */
	public AdapterStrategy1450(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	@Override
	public Adapter<?, ?> getAdapter(String className) {
		return adapters.get(className);
	}

	@Override
	public boolean shouldAdapt(String className) {
		if (!className.startsWith("no.uib.cipr")) { //$NON-NLS-1$
			return false;
		}
		try {
			Class<?> type = Class.forName(className, false, classLoader);
			return !type.isInterface();
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@Override
	public boolean createdThis(Object object) {
		return object.getClass().getClassLoader() == classLoader;
	}
	
	@Override
	public Class<?> resolveClass(String className) throws ClassNotFoundException {
		return Class.forName(className, false, classLoader);
	}
	
	/**
	 * Duck-typing interface to facilitate manipulating legacy DenseMatrix.
	 */
	interface MatrixProxy {
		int numRows();
		int numColumns();
		double[] getData();
	}
}
