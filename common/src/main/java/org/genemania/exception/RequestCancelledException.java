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
 * RequestCancelledException 
 * Created Jan 13, 2010
 * @author Ovi Comes
 */
package org.genemania.exception;

public class RequestCancelledException extends Exception {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = 5793457940825144002L;
	
	// __[attributes]__________________________________________________________
	public int errorCode;

	// __[constructors]________________________________________________________
	public RequestCancelledException() {
		super();
	}

	public RequestCancelledException(String msg, int errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}

	public RequestCancelledException(Throwable t) {
		super(t);
	}

	public RequestCancelledException(String msg) {
		super(msg);
	}

	public RequestCancelledException(int errorCode) {
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
