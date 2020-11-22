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

import java.util.LinkedList;
import java.util.List;

/**
 * Simple Parser for a JSP.
 * 
 * Parses a Jsp-Source into a list of tags for further processing. Please note:
 * plain content is also parsed into a special tag-object, so processing all
 * tags is straight forward.
 * 
 * @author Maik Jablonski
 */
public class Parser {

	public static class JspParserException extends Exception {
		public JspParserException(String msg) {
			super(msg);
		}
	}

	private final Tag[] TAG_REGISTRY = new Tag[] { new Comment(), new Directive(), new Declaration(), new Expression(), new Scriptlet() };

	private List<Tag> tags = new LinkedList<Tag>();
	private Tag currentTag;
	private String source;
	private int currentSourcePosition;

	/**
	 * @param source Jsp-Source
	 * @throws JspParserException
	 */
	public Parser(String source) throws JspParserException {
		this.source = source;
		parse();
	}

	/**
	 * @return List of parsed Tag-objects.
	 */
	public List<Tag> getTags() {
		return tags;
	}

	private void parse() throws JspParserException {
		currentTag = new Content();

		// scan until at end of data
		for (int start = 0; currentSourcePosition < source.length();) {
			int nextOpenTagPosition = source.indexOf('<', start);

			if (nextOpenTagPosition == -1) {
				// we're done.
				currentTag.setBody(source.substring(currentSourcePosition));
				tags.add(currentTag);
				return;
			}

			// check to see if this is the start of a block we care about
			if (scanForBlockStart(nextOpenTagPosition)) {
				start = currentSourcePosition;
			} else {
				start = nextOpenTagPosition + 1;
			}
		}
	}

	private boolean scanForBlockStart(int offset) throws JspParserException {
		for (Tag tag : TAG_REGISTRY) {
			if (isBlockStart(tag.getOpenTag(), offset)) {
				// we have a new block. finish the existing block
				currentTag.setBody(source.substring(currentSourcePosition, offset));
				tags.add(currentTag);

				currentSourcePosition = offset + tag.getOpenTag().length();

				// create a new parse element based on the found class
				try {
					currentTag = tag.getClass().newInstance();
				} catch (Exception e) {
					System.exit(1);
				}

				// scan for the end of the block
				String block = getParseBlock(currentTag);

				// record this parse element
				currentTag.setBody(block);
				tags.add(currentTag);

				// finished jsp block, back to an HTML block
				currentTag = new Content();
				return true;
			}
		}
		return false;
	}

	// return true if offset into input data points to the given
	// block start tag
	private boolean isBlockStart(String tag, int offset) {
		int tagLength = tag.length();
		if (source.length() - offset > tagLength) {
			String subs = source.substring(offset, offset + tagLength);
			return tag.equalsIgnoreCase(subs);
		}
		return false;
	}

	// scan to end of JSP tag, adding data to block
	private String getParseBlock(Tag tag) throws JspParserException {
		String endTag = tag.getCloseTag();
		int endTagLength = endTag.length();

		// scan protentially as far as the end of the input data
		int start = currentSourcePosition;
		while (currentSourcePosition < source.length()) {
			// get the index of the next end tag
			int nextEndTagPosition = source.indexOf('>', currentSourcePosition);

			if (nextEndTagPosition == -1) {
				throw new JspParserException("Error: expecting '" + endTag + "'");
			}

			// found an end tag
			String endTagName = source.substring(nextEndTagPosition - endTagLength + 1, nextEndTagPosition + 1);

			// is it the end tag we're looking for
			if ((nextEndTagPosition - endTagLength > 0) && endTag.equalsIgnoreCase(endTagName)) {
				// yes, found the end of the jsp block.
				currentSourcePosition = nextEndTagPosition + 1;
				return source.substring(start, (nextEndTagPosition - endTagLength + 1));
			}

			// otherwise keep looking
			currentSourcePosition = nextEndTagPosition + 1;
		}
		throw new JspParserException("Error: expecting '" + endTag);
	}
}
