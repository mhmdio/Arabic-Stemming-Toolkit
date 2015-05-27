/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is TRECFullUTFTokenizer.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald (craigm{at}dcs.gla.ac.uk)
 */

package org.terrier.indexing;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.terrier.utility.TagSet;
/**
 * This is a subclass of TRECFullTokenizer, which is less restrictive than it's parent. In this class any character passing Character.isLetterOrDigit() is accepted as a valid query term.
 * 
 * @author Craig Macdonald
 * @since 2.1
 * 
 * @deprecated From 3.5, {@link TRECFullTokenizer} should be used instead, with
 * <tt>trec.encoding</tt> set to <tt>utf8</tt>.
 */
public class TRECFullUTFTokenizer extends TRECFullTokenizer
{
	/**
	 * Constructs an instance of the TRECFullUTFTokenizer.
	 */
	public TRECFullUTFTokenizer() { super(); }
	/**
	 * Constructs an instance of the TRECFullUTFTokenizer, given a BufferReader.
	 * @param br
	 */
	public TRECFullUTFTokenizer(BufferedReader br) { super(br); }
	/** 
	 * Constructs an instance of the TRECFullUTFTokenizer.
	 * @param _tagSet
	 * @param _exactSet
	 */
	public TRECFullUTFTokenizer(TagSet _tagSet, TagSet _exactSet) { super(_tagSet, _exactSet); }
	/** 
	 * Constructs an instance of the TRECFullUTFTokenizer.
	 * @param _ts
	 * @param _exactSet
	 * @param br
	 */
	public TRECFullUTFTokenizer(TagSet _ts, TagSet _exactSet, BufferedReader br) { super(_ts,_exactSet,br); }

	/**
	 * A restricted check function for discarding uncommon, or 'strange' terms.
	 * @param s The term to check.
	 * @return the term if it passed the check, otherwise null.
	 */
	protected String check(String s) {
		//if the s is null
		//or if it is longer than a specified length
		if (s == null)
			return null;
		final int length = s.length();
		if (length == 0 || length > tokenMaximumLength)
			return null;
		if (!stk.empty() && exactTagSet.isTagToProcess((String) stk.peek()))
			return s;
		final StringReader sr = new StringReader(s);
		int counter = 0;
		int counterdigit = 0;
		int ch = -1;
		int chNew = -1;
		try {
			while ((chNew = sr.read()) != -1 && counter <= 2) {
				if (Character.isDigit((char)chNew))
					counterdigit++;
				if (ch == chNew)
					counter++;
				else
					counter = 1;
				ch = chNew;
			}
			sr.close();
		} catch (IOException ioe) { /* we're reading a string, this should never happen */}
		//if it contains more than 4 consequtive same letters,
		//or more than 4 digits, then discard the term.
		if (counter > 3 | counterdigit > 4)
			return null;
		return s;
	}

/**
	 * nextTermWithNumbers gives the first next string which is not a tag. All
	 * encounterd tags are pushed or popped according they are initial or final
	 */
	public String nextToken() {
		//the string to return as a result at the end of this method.
		String s = null;
		StringWriter sw = null;
		//are we in a body of a tag?
		boolean btag = true;
		//stringBuffer.delete(0, stringBuffer.length());
		int ch = 0;
		//are we reading the document number?
		boolean docnumber = false;
		//while not the end of document, or the end of file, or we are in a tag
		while (btag && ch != -1 & !EOD) {
			sw = new StringWriter();
			boolean tag_f = false;
			boolean tag_i = false;
			error = false;
			try {
				if (lastChar == 60)
					ch = lastChar;
				//Modified by G.Amati 7 june 2002
				//Removed a check: ch!=62
				//If not EOF and ch.isNotALetter and ch.isNotADigit and
				// ch.isNot '<' and ch.isNot '&'
				while (ch != -1 && ! Character.isLetterOrDigit(ch) &&  ch != '<' && ch != '&') {
					ch = br.read();
					counter++;
					//if ch is '>' (end of tag), then there is an error.
					if (ch == '>')
						error = true;
				}
				//if a tag begins
				if (ch == '<') {
					ch = br.read();
					counter++;
					//if it is a closing tag, set tag_f true
					if (ch == '/') {
						ch = br.read();
						counter++;
						tag_f = true;
					} else if (ch == '!') { //else if it is a comment, that is
										   // <!
						//read until you encounter a '<', or a '>', or the end
						// of file
						while ((ch = br.read()) != '>' && ch != '<' && ch != -1) {
							counter++;
						}
						counter++;
					} else
						tag_i = true; //otherwise, it is an opening tag
				}
				//Modified by V.Plachouras to take into account the exact tags
				if (ch == '&' && !stk.empty()
						&& !exactTagSet.isTagToProcess((String) stk.peek())) {
					//Modified by G.Amati 7 june 2002 */
					//read until an opening or the end of a tag is encountered,
					// or the end of file, or a space, or a semicolon,
					//which means the end of the escape sequence &xxx;
					while ((ch = br.read()) != '>' && ch != '<' && ch != -1
							&& ch != ' ' && ch != ';') {
						counter++;
					}
					counter++;
				}
				//ignore all the spaces encountered
				while (ch == ' ') {
					ch = br.read();
					counter++;
				}
				//if the body of a tag is encountered
				if ((btag = (tag_f || tag_i))) {
					//read until the end of file, or the start, or the end of a
					// tag, and save the content of the tag
					while (ch != -1 && ch != '<' && ch != '>') {
						sw.write(ch);
						ch = br.read();
						counter++;
					}
				} else { //otherwise, if we are not in the body of a tag
					if (!stk.empty())
						docnumber = inDocnoTag(); // //check if we are in a DOCNO tag
					//if we are in a DOCNO tag
					if (docnumber) {
						//read and save the characters until encountering a '<'
						// or a '>'
						while (ch != -1 && ch != '<' && ch != '>') {
							sw.write(ch);
							ch = br.read();
							counter++;
						}
					}
					if (!stk.empty() && !exactTagSet.isTagToProcess((String) stk.peek())) {
						//read a sequence of letters or digits.
						while (ch != -1 && Character.isLetterOrDigit(ch)) {
							//transforms the uppercase character to lowercase
							if (lowercase) 
								ch = Character.toLowerCase(ch);
							sw.write(ch);
							ch = br.read();
							counter++;
						}
					} else {
						//read a sequence of letters or digits.
						while (ch != -1 && (ch == '&' || Character.isLetterOrDigit(ch))) {
							//transforms the uppercase character to lowercase
							if (lowercase) 
								ch = Character.toLowerCase(ch);
							sw.write(ch);
							ch = br.read();
							counter++;
						}
					}
				}
				lastChar = ch;
				s = sw.toString();
				sw.close();
				if (tag_i) {
					if ((tagSet.isTagToProcess(s) || tagSet.isTagToSkip(s))
							&& !s.equals("")) {
						stk.push(s);
						if (tagSet.isTagToProcess(s)) {
							inTagToProcess = true;
							inTagToSkip = false;
							if (tagSet.isIdTag(s))
								inDocnoTag = true;
							else
								inDocnoTag = false;
						} else {
							inTagToSkip = true;
							inTagToProcess = false;
						}
					}
					return null;
				}
				if (tag_f) {
					if ((tagSet.isTagToProcess(s) || tagSet.isTagToSkip(s))
							&& !s.equals("")) {
						processEndOfTag(s);
						String stackTop = null;
						if (!stk.isEmpty()) {
							stackTop = (String) stk.peek();
							if (tagSet.isTagToProcess(stackTop)) {
								inTagToProcess = true;
								inTagToSkip = false;
								if (tagSet.isIdTag(stackTop))
									inDocnoTag = true;
								else
									inDocnoTag = false;
							} else {
								inTagToProcess = false;
								inTagToSkip = true;
								inDocnoTag = false;
							}
						} else {
							inTagToProcess = false;
							inTagToSkip = false;
							inDocnoTag = false;
						}
					}
					return null;
				}
			} catch (IOException ioe) {
				logger.fatal("Input/Output exception during reading tokens.", ioe);
				
			}
		}
		if (ch == -1) {
			EOF = true;
			EOD = true;
		}
		//if the token is not a tag or a document number, then check whether it
		// should be removed or not.
		if (!btag & !docnumber)
			return check(s);
		else
			return s;
	}

}
