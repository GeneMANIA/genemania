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

package org.genemania.plugin.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

public abstract class AbstractXMLReader implements XMLReader {
	protected static final String TYPE_STRING = "xsd:string"; //$NON-NLS-1$
	protected static final String TYPE_NUMBER = "xsd:number"; //$NON-NLS-1$

	private ContentHandler contentHandler;
	private DTDHandler dtdHandler;
	private EntityResolver entityResolver;
	private ErrorHandler errorHandler;
	private Attributes emptyAttributes = new AttributesImpl();
	
	public ContentHandler getContentHandler() {
		return contentHandler;
	}

	public DTDHandler getDTDHandler() {
		return dtdHandler;
	}

	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotSupportedException(name);
	}

	public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotSupportedException(name);
	}

	public void parse(String systemId) throws IOException, SAXException {
		parse(new InputSource(systemId));
	}

	public void setContentHandler(ContentHandler handler) {
		contentHandler = handler;
	}

	public void setDTDHandler(DTDHandler handler) {
		dtdHandler = handler;
	}

	public void setEntityResolver(EntityResolver resolver) {
		entityResolver = resolver;
	}

	public void setErrorHandler(ErrorHandler handler) {
		errorHandler = handler;
	}

	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotSupportedException(name);
	}

	public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new SAXNotSupportedException(name);
	}
	
	public void serialize(OutputStream stream) throws SAXException {
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setAttribute("indent-number", new Integer(2)); //$NON-NLS-1$
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
			
			StreamResult result = new StreamResult(new OutputStreamWriter(stream, "utf-8")); //$NON-NLS-1$
			SAXSource source = new SAXSource(this , new InputSource());
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			throw new SAXException(e);
		} catch (TransformerException e) {
			throw new SAXException(e);
		} catch (UnsupportedEncodingException e) {
			throw new SAXException(e);
		}
	}
	
	public void startElement(String name, Attributes attributes) throws SAXException {
		contentHandler.startElement("", name, name, attributes); //$NON-NLS-1$
	}

	public void startElement(String name) throws SAXException {
		contentHandler.startElement("", name, name, emptyAttributes); //$NON-NLS-1$
	}
	
	public void simpleElement(String name, String contents) throws SAXException {
		contentHandler.startElement("", name, name, emptyAttributes); //$NON-NLS-1$
		try {
			if (contents != null && contents.length() > 0) {
				contentHandler.characters(contents.toCharArray(), 0, contents.length());
			}
		} finally {
			contentHandler.endElement("", name, name); //$NON-NLS-1$
		}
	}

	public void simpleElement(String name, Attributes attributes) throws SAXException {
		contentHandler.startElement("", name, name, attributes); //$NON-NLS-1$
		contentHandler.endElement("", name, name); //$NON-NLS-1$
	}

	public void endElement(String name) throws SAXException {
		contentHandler.endElement("", name, name); //$NON-NLS-1$
	}
	
	public void startDocument() throws SAXException {
		contentHandler.startDocument();
	}
	
	public void endDocument() throws SAXException {
		contentHandler.endDocument();
	}
}
