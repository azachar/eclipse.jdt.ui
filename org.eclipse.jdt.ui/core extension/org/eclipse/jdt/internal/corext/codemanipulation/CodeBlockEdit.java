/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.corext.codemanipulation;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.internal.corext.Assert;
import org.eclipse.jdt.internal.corext.textmanipulation.SimpleTextEdit;
import org.eclipse.jdt.internal.corext.textmanipulation.TextBuffer;
import org.eclipse.jdt.internal.corext.textmanipulation.TextBufferEditor;
import org.eclipse.jdt.internal.corext.textmanipulation.TextEdit;
import org.eclipse.jdt.internal.corext.textmanipulation.TextRange;
import org.eclipse.jdt.internal.corext.textmanipulation.TextRegion;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.Strings;

public final class CodeBlockEdit extends TextEdit {

	private static class UndoEdit extends SimpleTextEdit {
		public UndoEdit(TextRange range, String text) {
			super(range, text);
		}
		public TextEdit copy() throws CoreException {
			return new UndoEdit(this.getTextRange(), getText());
		}
	}

	private TextRange fRange;
	private AbstractCodeBlock fBlock;
	private int fIndent;

	public static CodeBlockEdit createReplace(int offset, int length, AbstractCodeBlock block, int indent) {
		return new CodeBlockEdit(new TextRange(offset, length), block, indent);
	}

	public static CodeBlockEdit createReplace(int offset, int length, AbstractCodeBlock block) {
		return new CodeBlockEdit(new TextRange(offset, length), block, -1);
	}
	
	public static CodeBlockEdit createInsert(int offset, AbstractCodeBlock block) {
		return new CodeBlockEdit(new TextRange(offset, 0), block, -1);
	}

	private CodeBlockEdit(TextRange range, AbstractCodeBlock block, int indent) {
		Assert.isNotNull(range);
		Assert.isNotNull(block);
		fRange= range;
		fBlock= block;
		fIndent= indent;
	}

	public TextEdit copy() throws CoreException {
		return new CodeBlockEdit(fRange.copy(), fBlock, fIndent);
	}

	/* non Java-doc
	 * @see TextEdit#connect
	 */
	public void connect(TextBufferEditor editor) throws CoreException {		
		final TextBuffer buffer= editor.getTextBuffer();
		final int offset= fRange.getOffset();
		final int end= offset + fRange.getLength();
		int lineOffset= buffer.getLineInformationOfOffset(end).getOffset();
		if (lineOffset == end) {
			int lineNumber= buffer.getLineOfOffset(lineOffset);
			if (lineNumber > 0) {
				fRange= new TextRange(offset, fRange.getLength() - buffer.getLineDelimiter(lineNumber - 1).length());
			}
		}
	}
	
	/* non Java-doc
	 * @see TextEdit#getTextRange
	 */
	public TextRange getTextRange() {
		return fRange;
	}
	
	/* non Java-doc
	 * @see TextEdit#doPerform
	 */
	public final TextEdit perform(TextBuffer buffer) throws CoreException {
		String current= buffer.getContent(fRange.getOffset(), fRange.getLength());
		buffer.replace(fRange, createText(buffer));
		return new UndoEdit(fRange, current);
	}	
	
	private String createText(TextBuffer buffer) {
		int tabWidth= CodeFormatterUtil.getTabWidth();
		final int offset= fRange.getOffset();
		final int firstLine= buffer.getLineOfOffset(offset);
		final TextRegion region= buffer.getLineInformation(firstLine);
		
		String firstLineIndent= CodeFormatterUtil.createIndentString(
			Strings.computeIndent(buffer.getContent(offset, region.getLength() - (offset - region.getOffset())), tabWidth));
		
		if (fIndent < 0) {
			fIndent= Strings.computeIndent(buffer.getLineContent(firstLine), tabWidth);
		}
		String indent= CodeFormatterUtil.createIndentString(fIndent);
		String delimiter= buffer.getLineDelimiter(firstLine);
		StringBuffer result= new StringBuffer();
		fBlock.fill(result, firstLineIndent, indent, delimiter);
		return result.toString();
	}
}
