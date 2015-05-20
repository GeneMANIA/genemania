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

package org.genemania.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.genemania.util.ProgressReporter;

import ch.enterag.utils.zip.FileEntry;
import ch.enterag.utils.zip.Zip64File;

/**
 * Convenience functions for downloading and manipulating files from the
 * GeneMANIA server.
 */
public class FileUtils {
	
	public static final String DEFAULT_BASE_URL = "http://www.genemania.org/plugin"; //$NON-NLS-1$
	public static final String DEFAULT_METADATA_URL = DEFAULT_BASE_URL + "/versions.txt"; //$NON-NLS-1$
	
	public static final String DESCRIPTION_FILE = "descriptions.txt"; //$NON-NLS-1$
	public static final String SIZE_FILE = "sizes.txt"; //$NON-NLS-1$
	
	private static final int BUFFER_SIZE = 1048576;
	
	public File download(URL dataUrl, File destination, ProgressReporter progress) throws IOException {
		HttpURLConnection.setFollowRedirects(true);
		URLConnection connection = getUrlConnection(dataUrl);
		connection.setUseCaches(false);
		
		int totalBytes = connection.getContentLength();
		progress.setMaximumProgress(100);
		InputStream input = connection.getInputStream();
		String fileName = determineFileName(connection, destination);
		String fullPath = join(File.separator, new String[] { destination.getPath(), fileName });

		File outFile = new File(fullPath);
		outFile.getParentFile().mkdirs();
		outFile.createNewFile();
		FileOutputStream output = new FileOutputStream(outFile);
		try {
			try {
				byte[] buffer = new byte[BUFFER_SIZE];
				int length = input.read(buffer);
				long totalRead = 0;
				while (length != -1) {
					if (progress.isCanceled()) {
						return null;
					}
					output.write(buffer, 0, length);
					totalRead += length;
					if (totalBytes == -1) {
						progress.setDescription(String.format(Strings.downloadProgress2_status, totalRead / 1000));
					} else {
						progress.setProgress((int) (100.0 * totalRead / totalBytes));
						progress.setDescription(String.format(Strings.downloadProgress_status, totalRead / 1000, totalBytes / 1000));
					}
					length = input.read(buffer);
				}
			} finally {
				output.close();
				if (progress.isCanceled()) {
					outFile.delete();
				}
			}
		} finally {
			input.close();
			progress.setDescription(""); //$NON-NLS-1$
		}
		return new File(fullPath);
	}
	
	public URLConnection getUrlConnection(URL url) throws IOException {
		return url.openConnection();
	}

	String determineFileName(URLConnection connection, File basePath) throws IOException {
		String contentDisposition = connection.getHeaderField("Content-Disposition"); //$NON-NLS-1$
		if (contentDisposition != null) {
			int index = contentDisposition.indexOf("filename="); //$NON-NLS-1$
			if (index > -1) {
				String fileName = contentDisposition.substring(index + "filename=".length()); //$NON-NLS-1$
				if (fileName.length() >= 2 && fileName.startsWith("\"") && fileName.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
					fileName = fileName.substring(1, fileName.length() - 1);
				}
				if (fileName.length() > 0) {
					return fileName;
				}
			}
		}
		String[] parts = connection.getURL().getPath().split("/"); //$NON-NLS-1$
		if (parts.length == 0) {
			throw new IOException();
		}
		return parts[parts.length - 1];
	}
	
	Properties getMetadata(URL url) throws IOException {
		URLConnection connection = getUrlConnection(url);
		InputStream metadataStream = connection.getInputStream();
		try {
			Properties properties = new Properties();
			properties.load(metadataStream);
			return properties;
		} finally {
			metadataStream.close();
		}
	}
	
	String join(String conjunct, String[] parts) {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i != 0) {
				buffer.append(conjunct);
			}
			buffer.append(parts[i]);
		}
		return buffer.toString();
	}
	
	@SuppressWarnings("unchecked")
	public void unzip(File zipPath, File destination, ProgressReporter progress) throws IOException {
		Zip64File file = new Zip64File(zipPath);
		try {
			int total = file.getFileEntries();
			progress.setMaximumProgress(100);
			List<FileEntry> entries = file.getListFileEntries();
			int processed = 0;
			progress.setStatus(String.format(Strings.unzip_status, zipPath.getName()));
			for (FileEntry entry : entries) {
				if (progress.isCanceled()) {
					return;
				}
				processed++;
				progress.setProgress((int) (100.0 * processed / total));
				
				String name = entry.getName();
				progress.setDescription(String.format("%s", name)); //$NON-NLS-1$
				
				String entryPath = String.format("%s%s%s", destination.getPath(), File.separator, name); //$NON-NLS-1$
				File entryFile = new File(entryPath);
				if (entry.isDirectory()) {
					entryFile.mkdirs();
				} else {
					entryFile.getParentFile().mkdirs();
					entryFile.createNewFile();
					write(entryFile, file.openEntryInputStream(name));
				}
			}
		} finally {
			file.close();
		}
	}

	private void write(File destination, InputStream in) throws IOException {
		try {
			FileOutputStream out = new FileOutputStream(destination);
			try {
				byte[] buffer = new byte[BUFFER_SIZE];
				int length = in.read(buffer, 0, buffer.length);
				while (length != -1) {
					out.write(buffer, 0, length);
					length = in.read(buffer, 0, buffer.length);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}
	
	public void delete(File path) throws IOException {
		if (path.isFile()) {
			path.delete();
			return;
		}
		
		if (path.isDirectory()) {
			for (File file : path.listFiles()) {
				delete(file);
			}
			path.delete();
		}
	}
	
	public Reader getUncompressedReader(String path) throws IOException {
		return getUncompressedReader(new File(path));
	}

	public Reader getUncompressedReader(File file) throws IOException {
		return new InputStreamReader(getUncompressedStream(file));
	}

	public InputStream getUncompressedStream(File file) throws IOException {
		try {
			InputStream stream = new GZIPInputStream(new FileInputStream(file));
			try {
				stream.read();
				return new GZIPInputStream(new FileInputStream(file));
			} finally {
				stream.close();
			}
		} catch (IOException e) {
		}
		return new FileInputStream(file);
	}
	
	public List<String> getCompatibleDataSets(String baseUrl, String schemaVersion) throws IOException {
		HttpURLConnection.setFollowRedirects(true);

		String url = String.format("%s/data/schema-%s.txt", baseUrl, schemaVersion); //$NON-NLS-1$
		URLConnection connection = getUrlConnection(new URL(url));
		connection.setUseCaches(false);
		List<String> dataSets = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		try {
			String line = reader.readLine();
			while (line != null) {
				dataSets.add(line);
				line = reader.readLine();
			}
			
		} finally {
			reader.close();
		}
		return dataSets;
	}
	
	public String findDataSetBaseUrl(String baseUrl, String dataId) {
		return String.format("%s/data/gmdata-%s", baseUrl, dataId); //$NON-NLS-1$
	}
	
	public Map<String, String> getDataSetDescriptions(String baseUrl) throws IOException {
		return getDataSetProperties(baseUrl, DESCRIPTION_FILE);
	}
	
	public Map<String, Long> getDataSetSizes(String baseUrl) throws IOException {
		Map<String, String> properties = getDataSetProperties(baseUrl, SIZE_FILE);
		Map<String, Long> sizes = new HashMap<String, Long>();
		
		for (Entry<String, String> entry : properties.entrySet()) {
			try {
				sizes.put(entry.getKey(), Long.decode(entry.getValue()));
			} catch (NumberFormatException e) {
				log(e);
				throw new IOException("Invalid Data Set", e);
			}
		}
			
		return sizes;
	}
	
	Map<String, String> getDataSetProperties(String baseUrl, String name) throws IOException {
		HttpURLConnection.setFollowRedirects(true);

		String url = String.format("%s/data/%s", baseUrl, name); //$NON-NLS-1$
		URLConnection connection = getUrlConnection(new URL(url));
		connection.setUseCaches(false);
		
		return getDataSetDescriptions(new InputStreamReader(connection.getInputStream()));
	}
	
	Map<String, String> getDataSetDescriptions(Reader source) throws IOException {
		Pattern pattern = Pattern.compile("([^#].*?)\\s*=\\s*(.*?)\\s*(#.*)?"); //$NON-NLS-1$
		Map<String, String> descriptions = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(source);
		try {
			String line = reader.readLine();
			while (line != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					descriptions.put(matcher.group(1), matcher.group(2));
				}
				line = reader.readLine();
			}
		} finally {
			reader.close();
		}
		return descriptions;
	}

	public File getUserHome() {
		return new File(System.getProperty("user.home")); //$NON-NLS-1$
	}
	
	public void log(Throwable e) {
		if (e != null) {
			Logger logger = Logger.getLogger(getClass());
			logger.error("Unexpected error", e); //$NON-NLS-1$
		}
	}
}
