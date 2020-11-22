/*
    Copyright (C) 2013 maik.jablonski@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package jfix.compiler.jsp;

import jfix.compiler.jsp.tag.*;

import java.util.StringTokenizer;

/**
 * Generates Java-Code in-memory from a JspParser.
 * 
 * You can get the result via calling <code>{@link #toString()}</code>.
 * 
 * @author Maik Jablonski
 */
public class Generator {

	private StringBuilder sb = new StringBuilder(8192);

	/**
	 * @param jspParser
	 *            an instance of a JspParser
	 * @param qualifiedClass
	 *            the full qualified java name of the class to be generated
	 */
	public Generator(Parser jspParser, String qualifiedClass) {
		String extendsClass = null;
		String bufferBytes = "8192";

		// Create a package declaration of qualified class name.
		if (qualifiedClass.indexOf(".") != -1) {
			sb.append("package " + qualifiedClass.substring(0, qualifiedClass.lastIndexOf(".")) + ";\n");
		}

		// Process directives for class-header
		for (Tag tag : jspParser.getTags()) {
			if (tag instanceof Directive) {
				StringTokenizer tokens = new StringTokenizer(tag.getBody(), " \t\n");
				while (tokens.hasMoreElements()) {
					String directive = tokens.nextToken();
					int eq = directive.indexOf('=');
					if (eq != -1) {
						String name = directive.substring(0, eq);
						String value = directive.substring(eq + 2, directive.length() - 1);

						// Derive generated class from another class?
						if ("extends".equalsIgnoreCase(name)) {
							extendsClass = value;
						}

						// Sets the internal StringBuffer to specified value.
						if ("buffer".equalsIgnoreCase(name)) {
							bufferBytes = value;
						}

						// Create import statements.
						if ("import".equalsIgnoreCase(name)) {
							for (String jspImport : value.split(",")) {
								sb.append("import " + jspImport + ";\n");
							}
							sb.append("\n");
						}
					}
				}
			}
		}

		// Create class-header.
		sb.append("public class " + qualifiedClass.substring(qualifiedClass.lastIndexOf(".") + 1));
		if (extendsClass != null) {
			sb.append(" extends " + extendsClass);
		}
		sb.append(" {\n");
		sb.append("\n");

		// Create all declarations.
		for (Tag tag : jspParser.getTags()) {
			if (tag instanceof Declaration) {
				StringTokenizer tokens = new StringTokenizer(tag.getBody(), "\n\r");
				while (tokens.hasMoreElements()) {
					sb.append("\t" + tokens.nextToken().trim() + "\n");
				}
			}
		}
		sb.append("\n");

		// Create toString() for class which is the worker and returns the
		// result.
		sb.append("\tpublic String toString() {\n");
		sb.append("\t\tjava.io.StringWriter out = new java.io.StringWriter(" + bufferBytes + ");\n");

		for (Tag tag : jspParser.getTags()) {
			// Process plain content.
			if (tag instanceof Content) {
				// Escape characters
				sb.append("\t\tout.write(\"");
				for (char character : tag.getBody().toCharArray()) {
					switch (character) {
					case '"':
						sb.append("\\\"");
						break;
					case '\n':
						// EOL reached, so start over with a new line...
						sb.append("\\n\");\n\t\tout.write(\"");
						break;
					case '\r':
						sb.append("\\r");
						break;
					case '\t':
						sb.append("\\t");
						break;
					default:
						sb.append(character);
					}
				}
				sb.append("\");\n");
			}

			// Process expressions.
			if (tag instanceof Expression) {
				sb.append("\t\tout.write(String.valueOf(" + tag.getBody().trim() + "));\n");
			}

			// Process scriptlets.
			if (tag instanceof Scriptlet) {
				StringTokenizer tokens = new StringTokenizer(tag.getBody(), "\n\r");
				while (tokens.hasMoreElements()) {
					sb.append("\t\t" + tokens.nextToken().trim() + "\n");
				}
			}
		}

		sb.append("\t\treturn out.toString();\n");
		sb.append("\t}\n");
		sb.append("\n");

		sb.append("}\n");
		sb.append("\n");
	}

	public String toString() {
		return sb.toString();
	}
}
