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
package org.genemania.plugin.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Version {
	private static final Pattern PATTERN = Pattern.compile("(\\d\\d\\d\\d-\\d\\d-\\d\\d)(-(.*))?"); //$NON-NLS-1$

	private final String baseVersion;
	private final String type;
	
	public Version(String baseVersion, String type) {
		this.baseVersion = baseVersion;
		this.type = type;
	}
	
	public String getBaseVersion() {
		return baseVersion;
	}
	
	public String getType() {
		return type;
	}
	
	public boolean isEquivalentTo(Version version) {
		return baseVersion.equals(version.baseVersion);
	}
	
	@Override
	public boolean equals(Object other) {
		Version version = (Version) other;
		return baseVersion.equals(version.baseVersion) && (type == null && version.type == null || type != null && type.equals(version.type));
	}
	
	@Override
	public int hashCode() {
		int result = baseVersion.hashCode();
		if (type == null) {
			return result;
		}
		return result + 23 * type.hashCode();
	}
	
	@Override
	public String toString() {
		if (type == null) {
			return baseVersion;
		}
		return baseVersion + "-" + type; //$NON-NLS-1$
	}
	
	public static Version parse(String string) {
		Matcher matcher = PATTERN.matcher(string);
		if (matcher.matches()) {
			return new Version(matcher.group(1), matcher.group(3));
		}
		return new Version(string, null);
	}
}
