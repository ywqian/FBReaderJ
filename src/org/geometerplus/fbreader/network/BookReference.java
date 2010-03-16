/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.network;

import java.io.File;
import java.net.URI;

import org.geometerplus.fbreader.Constants;

public class BookReference {

	public interface Type {
		int UNKNOWN = 0; // Unknown reference type
		int DOWNLOAD_FULL = 1; // reference for download full version of the book
		int DOWNLOAD_FULL_CONDITIONAL = 2; // reference for download full version of the book, useful only when
		int DOWNLOAD_DEMO = 3;
		int DOWNLOAD_FULL_OR_DEMO = 4;
		int BUY = 5;
		int BUY_IN_BROWSER = 6;
	}

	public interface Format {
		int NONE = 0;
		int MOBIPOCKET = 1;
		int FB2_ZIP = 2;
		int EPUB = 3;
	}

	public final String URL;
	public final int BookFormat;
	public final int ReferenceType;

	public BookReference(String url, int format, int type) {
		URL = url;
		BookFormat = format;
		ReferenceType = type;
	}

	// returns clean URL without any account/user-specific parts
	public String cleanURL() {
		return URL;
	}

	public static String makeBookFileName(String url, int format, int type) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (java.net.URISyntaxException ex) {
			return null;
		}

		String host = uri.getHost();

		StringBuilder path = new StringBuilder(host);
		if (host.startsWith("www.")) {
			path.delete(0, 4);
		}
		path.append(File.separator);
		path.insert(0, File.separator);
		path.insert(0, Constants.BOOKS_DIRECTORY);

		int index = path.length();

		path.append(uri.getPath());

		int nameIndex = index;
		while (index < path.length()) {
			char ch = path.charAt(index);
			if (ch == '<' || ch == '>' || ch == ':' || ch == '"' || ch == '|' || ch == '?' || ch == '*' || ch == '\\') {
				path.setCharAt(index, '_');
			}
			if (ch == '/') {
				if (index + 1 == path.length()) {
					path.deleteCharAt(index);
				} else {
					path.setCharAt(index, File.separatorChar);
					nameIndex = index + 1;
				}
			}
			++index;
		}

		String ext = null;
		switch (format) {
			case Format.EPUB:
				ext = ".epub";
				break;
			case Format.MOBIPOCKET:
				ext = ".mobi";
				break;
			case Format.FB2_ZIP:
				ext = ".fb2.zip";
				break;
		}

		if (ext == null) {
			int j = path.lastIndexOf(".");
			if (j > nameIndex) {
				ext = path.substring(j);
				path.delete(j, path.length());
			}
		} else if (path.length() > ext.length() && path.substring(path.length() - ext.length()).equals(ext)) {
			path.delete(path.length() - ext.length(), path.length());
		}

		String query = uri.getQuery();
		if (query != null) {
			index = 0;
			while (index < query.length()) {
				int j = query.indexOf("&", index);
				if (j == -1) {
					j = query.length();
				}
				String param = query.substring(index, j);
				if (!param.startsWith("username=")
					&& !param.startsWith("password=")
					&& !param.endsWith("=")) {
					int k = path.length();
					path.append("_").append(param);
					while (k < path.length()) {
						char ch = path.charAt(k);
						if (ch == '<' || ch == '>' || ch == ':' || ch == '"' || ch == '|' || ch == '?' || ch == '*' || ch == '\\' || ch == '/') {
							path.setCharAt(k, '_');
						}
						++k;
					}
				}
				index = j + 1;
			}
		}
		if (type == Type.DOWNLOAD_DEMO) {
			path.append(".trial");
		}
		return path.append(ext).toString();
	}

	public String localCopyFileName() {
		String fileName = makeBookFileName(cleanURL(), BookFormat, ReferenceType);
		if (fileName != null && new File(fileName).exists()) {
			return fileName;
		}
		return null;
	}

}
