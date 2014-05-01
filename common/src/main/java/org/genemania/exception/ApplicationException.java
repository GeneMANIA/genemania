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
 * ApplicationException 
 * Created Sep 22, 2008
 * @author Ovi Comes
 */
package org.genemania.exception;

import java.util.List;

import org.genemania.Constants;

public class ApplicationException extends Exception {

	// __[static]______________________________________________________________
	private static final long serialVersionUID = 7091789650708975848L;

	// __[attributes]__________________________________________________________
	private int errorCode;
	private String errorMessage;
	private List<Object> data;// additional error data

	// __[constructors]________________________________________________________
	public ApplicationException() {
		super();
	}

	public ApplicationException(Throwable t) {
		super(t);
		if (t instanceof ApplicationException) {
			setErrorCode(((ApplicationException) t).getErrorCode());
			setErrorMessage(((ApplicationException) t).getErrorMessage());
			setData(((ApplicationException) t).getData());
		} else {
			setErrorCode(Constants.ERROR_CODES.APPLICATION_ERROR);
			setErrorMessage(t.getMessage());
		}
	}

	public ApplicationException(String msg) {
		super(msg);
	}

	public ApplicationException(String msg, Throwable t) {
		super(msg, t);
	}

	public ApplicationException(int errorCode) {
		this.errorCode = errorCode;
	}

	public ApplicationException(String msg, int errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}

	// __[accessors]___________________________________________________________
	public List<Object> getData() {
		return data;
	}

	public void setData(List<Object> data) {
		this.data = data;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	// __[public helpers]______________________________________________________
	public String printError() {
		return "[" + errorCode + "] " + errorMessage; 
	}
}
