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
package jfix.compiler;

import jfix.compiler.jsp.Generator;
import jfix.compiler.jsp.Parser;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JSPCompiler {

    private static JavaCompiler javaCompiler = new JavaCompiler();

    private Class<?> classType;

    public JSPCompiler(Class<?> clazz) {
        this.classType = clazz;
    }

    public Object getObject() {
        return javaCompiler.eval(getJava());
    }

    public String getJSP() {
        String resource = String.format("/%s.jsp", classType.getName().replace(".class", "").replace(".", "/"));
        try (InputStream stream = getClass().getResourceAsStream(resource)) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String getJava() {
        try {
            return new Generator(new Parser(getJSP()), classType.getSimpleName()).toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
