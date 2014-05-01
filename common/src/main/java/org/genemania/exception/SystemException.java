/**
 * This file is part of GeneMANIA.
 * Copyright (C) 2010 University of Toronto.
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

/**
 * SystemException 
 * Created Oct 10, 2008
 * @author Ovi Comes
 */
package org.genemania.exception;

public class SystemException extends Exception {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = 4791500808465008308L;
	
	// __[attributes]__________________________________________________________
	private int errorCode;

	// __[constructors]________________________________________________________
	public SystemException() {
		super();
	}

	public SystemException(Throwable t) {
		super(t);
	}

	public SystemException(String msg) {
		super(msg);
	}

	public SystemException(int errorCode) {
		this.errorCode = errorCode;
	}

	public SystemException(String msg, int errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}

	// __[accessors]___________________________________________________________
	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}
