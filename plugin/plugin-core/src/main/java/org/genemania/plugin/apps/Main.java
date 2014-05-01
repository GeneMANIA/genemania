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
package org.genemania.plugin.apps;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("nls")
public class Main {
	public static void main(String[] args) throws Exception {
		Map<String, AppDescriptor> apps = new LinkedHashMap<String, AppDescriptor>();
		addApp(apps, new AppDescriptor(CrossValidator.class));
		addApp(apps, new AppDescriptor(DataAdmin.class));
		addApp(apps, new AppDescriptor(GeneSanitizer.class));
		addApp(apps, new AppDescriptor(IdImporter.class));
		addApp(apps, new AppDescriptor(NetworkAssessor.class));
		addApp(apps, new AppDescriptor(NetworkImporter.class));
		addApp(apps, new AppDescriptor(QueryRunner.class));
		addApp(apps, new AppDescriptor(ValidationSetMaker.class));
		
		if (args.length == 0) {
			printUsage(apps);
			return;
		}
		
		String appName = args[0];
		AppDescriptor descriptor = apps.get(appName);
		if (descriptor == null) {
			printUsage(apps);
			return;
		}
		
		String[] tail = Arrays.copyOfRange(args, 1, args.length);
		descriptor.run(tail);
	}
	
	private static void printUsage(Map<String, AppDescriptor> apps) {
		System.err.println("Usage: java -jar genemania.jar  app-name\n\n...where app-name is one of:");
		for (AppDescriptor descriptor : apps.values()) {
			System.err.printf("    %s\n", descriptor.type.getSimpleName());
		}
		return;
	}

	private static void addApp(Map<String, AppDescriptor> apps, AppDescriptor appDescriptor) {
		apps.put(appDescriptor.type.getSimpleName(), appDescriptor);
	}

	static class AppDescriptor {
		public Class<?> type;
		
		public AppDescriptor(Class<?> type) {
			this.type = type;
		}
		
		String getName() {
			return type.getSimpleName();
		}
		
		void run(String[] args) throws Exception {
			Method method = type.getDeclaredMethod("main", String[].class);
			method.invoke(null, (Object) args);
		}
	}
}
