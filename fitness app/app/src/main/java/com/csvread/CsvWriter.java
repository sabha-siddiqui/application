/*
 * Java CSV is a stream based library for reading and writing
 * CSV and other delimited data.
 *
 * Copyright (C) Bruce Dunwiddie bruce@csvreader.com
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package com.csvread;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.nio.charset.Charset;


public class CsvWriter {
	private Writer outputStream = null;

	private String fileName = null;

	private boolean firstColumn = true;

	private boolean useCustomRecordDelimiter = false;

	private Charset charset = null;
	private UserSettings userSettings = new UserSettings();

	private boolean initialized = false;

	private boolean closed = false;

	private String systemRecordDelimiter = System.getProperty("line.separator");

	public static final int ESCAPE_MODE_DOUBLED = 1;

	public static final int ESCAPE_MODE_BACKSLASH = 2;

	public CsvWriter(String fileName, char delimiter, Charset charset) {
		if (fileName == null) {
			throw new IllegalArgumentException("Parameter fileName can not be null.");
		}

		if (charset == null) {
			throw new IllegalArgumentException("Parameter charset can not be null.");
		}

		this.fileName = fileName;
		userSettings.Delimiter = delimiter;
		this.charset = charset;
	}

	public CsvWriter(String fileName) {
		this(fileName, Letters.COMMA, Charset.forName("ISO-8859-1"));
	}

	public CsvWriter(Writer outputStream, char delimiter) {
		if (outputStream == null) {
			throw new IllegalArgumentException("Parameter outputStream can not be null.");
		}

		this.outputStream = outputStream;
		userSettings.Delimiter = delimiter;
		initialized = true;
	}

	public CsvWriter(OutputStream outputStream, char delimiter, Charset charset) {
		this(new OutputStreamWriter(outputStream, charset), delimiter);
	}

	public char getDelimiter() {
		return userSettings.Delimiter;
	}

	public void setDelimiter(char delimiter) {
		userSettings.Delimiter = delimiter;
	}

	public char getRecordDelimiter() {
		return userSettings.RecordDelimiter;
	}

	public void setRecordDelimiter(char recordDelimiter) {
		useCustomRecordDelimiter = true;
		userSettings.RecordDelimiter = recordDelimiter;
	}

	public char getTextQualifier() {
		return userSettings.TextQualifier;
	}

	public void setTextQualifier(char textQualifier) {
		userSettings.TextQualifier = textQualifier;
	}

	public boolean getUseTextQualifier() {
		return userSettings.UseTextQualifier;
	}

	public void setUseTextQualifier(boolean useTextQualifier) {
		userSettings.UseTextQualifier = useTextQualifier;
	}

	public int getEscapeMode() {
		return userSettings.EscapeMode;
	}

	public void setEscapeMode(int escapeMode) {
		userSettings.EscapeMode = escapeMode;
	}

	public void setComment(char comment) {
		userSettings.Comment = comment;
	}

	public char getComment() {
		return userSettings.Comment;
	}

	public boolean getForceQualifier() {
		return userSettings.ForceQualifier;
	}

	public void setForceQualifier(boolean forceQualifier) {
		userSettings.ForceQualifier = forceQualifier;
	}

	public void write(String content, boolean preserveSpaces)
			throws IOException {
		checkClosed();

		checkInit();

		if (content == null) {
			content = "";
		}

		if (!firstColumn) {
			outputStream.write(userSettings.Delimiter);
		}

		boolean textQualify = userSettings.ForceQualifier;

		if (!preserveSpaces && content.length() > 0) {
			content = content.trim();
		}

		if (!textQualify
				&& userSettings.UseTextQualifier
				&& (content.indexOf(userSettings.TextQualifier) > -1
						|| content.indexOf(userSettings.Delimiter) > -1
						|| (!useCustomRecordDelimiter && (content
								.indexOf(Letters.LF) > -1 || content
								.indexOf(Letters.CR) > -1))
						|| (useCustomRecordDelimiter && content
								.indexOf(userSettings.RecordDelimiter) > -1)
						|| (firstColumn && content.length() > 0 && content
								.charAt(0) == userSettings.Comment) ||

				(firstColumn && content.length() == 0))) {
			textQualify = true;
		}

		if (userSettings.UseTextQualifier && !textQualify
				&& content.length() > 0 && preserveSpaces) {
			char firstLetter = content.charAt(0);

			if (firstLetter == Letters.SPACE || firstLetter == Letters.TAB) {
				textQualify = true;
			}

			if (!textQualify && content.length() > 1) {
				char lastLetter = content.charAt(content.length() - 1);

				if (lastLetter == Letters.SPACE || lastLetter == Letters.TAB) {
					textQualify = true;
				}
			}
		}

		if (textQualify) {
			outputStream.write(userSettings.TextQualifier);

			if (userSettings.EscapeMode == ESCAPE_MODE_BACKSLASH) {
				content = replace(content, "" + Letters.BACKSLASH, ""
						+ Letters.BACKSLASH + Letters.BACKSLASH);
				content = replace(content, "" + userSettings.TextQualifier, ""
						+ Letters.BACKSLASH + userSettings.TextQualifier);
			} else {
				content = replace(content, "" + userSettings.TextQualifier, ""
						+ userSettings.TextQualifier
						+ userSettings.TextQualifier);
			}
		} else if (userSettings.EscapeMode == ESCAPE_MODE_BACKSLASH) {
			content = replace(content, "" + Letters.BACKSLASH, ""
					+ Letters.BACKSLASH + Letters.BACKSLASH);
			content = replace(content, "" + userSettings.Delimiter, ""
					+ Letters.BACKSLASH + userSettings.Delimiter);

			if (useCustomRecordDelimiter) {
				content = replace(content, "" + userSettings.RecordDelimiter,
						"" + Letters.BACKSLASH + userSettings.RecordDelimiter);
			} else {
				content = replace(content, "" + Letters.CR, ""
						+ Letters.BACKSLASH + Letters.CR);
				content = replace(content, "" + Letters.LF, ""
						+ Letters.BACKSLASH + Letters.LF);
			}

			if (firstColumn && content.length() > 0
					&& content.charAt(0) == userSettings.Comment) {
				if (content.length() > 1) {
					content = "" + Letters.BACKSLASH + userSettings.Comment
							+ content.substring(1);
				} else {
					content = "" + Letters.BACKSLASH + userSettings.Comment;
				}
			}
		}

		outputStream.write(content);

		if (textQualify) {
			outputStream.write(userSettings.TextQualifier);
		}

		firstColumn = false;
	}

	public void write(String content) throws IOException {
		write(content, false);
	}

	public void writeComment(String commentText) throws IOException {
		checkClosed();

		checkInit();

		outputStream.write(userSettings.Comment);

		outputStream.write(commentText);

		if (useCustomRecordDelimiter) {
			outputStream.write(userSettings.RecordDelimiter);
		} else {
			outputStream.write(systemRecordDelimiter);
		}

		firstColumn = true;
	}

	public void writeRecord(String[] values, boolean preserveSpaces)
			throws IOException {
		if (values != null && values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				write(values[i], preserveSpaces);
			}

			endRecord();
		}
	}

	public void writeRecord(String[] values) throws IOException {
		writeRecord(values, false);
	}

	public void endRecord() throws IOException {
		checkClosed();

		checkInit();

		if (useCustomRecordDelimiter) {
			outputStream.write(userSettings.RecordDelimiter);
		} else {
			outputStream.write(systemRecordDelimiter);
		}

		firstColumn = true;
	}


	private void checkInit() throws IOException {
		if (!initialized) {
			if (fileName != null) {
				outputStream = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(fileName), charset));
			}

			initialized = true;
		}
	}

	public void flush() throws IOException {
		outputStream.flush();
	}
	public void close() {
		if (!closed) {
			close(true);

			closed = true;
		}
	}

	private void close(boolean closing) {
		if (!closed) {
			if (closing) {
				charset = null;
			}

			try {
				if (initialized) {
					outputStream.close();
				}
			} catch (Exception e) {

			}

			outputStream = null;

			closed = true;
		}
	}
	private void checkClosed() throws IOException {
		if (closed) {
			throw new IOException(
			"This instance of the CsvWriter class has already been closed.");
		}
	}
	protected void finalize() {
		close(false);
	}

	private class Letters {
		public static final char LF = '\n';

		public static final char CR = '\r';

		public static final char QUOTE = '"';

		public static final char COMMA = ',';

		public static final char SPACE = ' ';

		public static final char TAB = '\t';

		public static final char POUND = '#';

		public static final char BACKSLASH = '\\';

		public static final char NULL = '\0';
	}

	private class UserSettings {
		public char TextQualifier;

		public boolean UseTextQualifier;

		public char Delimiter;

		public char RecordDelimiter;

		public char Comment;

		public int EscapeMode;

		public boolean ForceQualifier;

		public UserSettings() {
			TextQualifier = Letters.QUOTE;
			UseTextQualifier = true;
			Delimiter = Letters.COMMA;
			RecordDelimiter = Letters.NULL;
			Comment = Letters.POUND;
			EscapeMode = ESCAPE_MODE_DOUBLED;
			ForceQualifier = false;
		}
	}

	public static String replace(String original, String pattern, String replace) {
		final int len = pattern.length();
		int found = original.indexOf(pattern);

		if (found > -1) {
			StringBuffer sb = new StringBuffer();
			int start = 0;

			while (found != -1) {
				sb.append(original.substring(start, found));
				sb.append(replace);
				start = found + len;
				found = original.indexOf(pattern, start);
			}

			sb.append(original.substring(start));

			return sb.toString();
		} else {
			return original;
		}
	}
}
