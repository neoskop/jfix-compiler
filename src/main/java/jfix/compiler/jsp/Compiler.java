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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * JspCompiler - compiles a JSP-source into a Java-code-file.
 * 
 * @author Maik Jablonski
 */
public class Compiler {

	public static void main(String[] args) throws Exception {
		for (String jspFile : args) {
			String clazzPath = jspFile.substring(0, jspFile.lastIndexOf("."));
			String qualifiedClass = clazzPath.replace('/', '.').replace('\\', '.');
			String javaFile = clazzPath + ".java";
			System.out.println(jspFile + " -> " + javaFile);

			// Read jsp-source from file
			File file = new File(jspFile);
			Reader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			char[] jspCode = new char[(int) file.length()];
			fileReader.read(jspCode);
			fileReader.close();

			// Generate java-source
			String javaCode = new Generator(new Parser(new String(jspCode)), qualifiedClass).toString();

			// Write java-source to file
			Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(javaFile), "UTF-8"));
			fileWriter.write(javaCode);
			fileWriter.close();
		}
	}
}
