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
 * ValidationException 
 * Created Oct 10, 2008
 * @author Ovi Comes
 */
package org.genemania.exception;

public class ValidationException extends Exception {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = 853319850391160009L;

	// __[attributes]__________________________________________________________
	public int errorCode;

	// __[constructors]________________________________________________________
	public ValidationException() {
		super();
	}

	public ValidationException(String msg, int errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}

	public ValidationException(Throwable t) {
		super(t);
	}

	public ValidationException(String msg) {
		super(msg);
	}

	public ValidationException(int errorCode) {
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
