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

import org.eclipse.jdt.internal.corext.util.Strings;

public abstract class AbstractCodeBlock {

	/**
	 * Fills the content of the code block into the given buffer using the given
	 * initial indent and and line separator.
	 */
	public void fill(StringBuffer buffer, String initialIndent, String lineSeparator) {
		fill(buffer, initialIndent, initialIndent, lineSeparator);
	}

	public abstract void fill(StringBuffer buffer, String firstLineIndent, String indent, String lineSeparator);

	public abstract boolean isEmpty();

	protected void fill(StringBuffer buffer, String code, String firstLineIndent, String indent, String lineSeparator) {
		String[] lines= Strings.convertIntoLines(code);
		final int lastLine= lines.length - 1;
		for (int i= 0; i < lines.length; i++) {
			if (i == 0)
				buffer.append(firstLineIndent);
			else
				buffer.append(indent);
			buffer.append(lines[i]);
			if (i < lastLine)
				buffer.append(lineSeparator);
		}
	}
}
