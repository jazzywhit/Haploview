/*
 * $Id: CheckDataException.java,v 1.1 2003/08/01 19:36:19 jmaller Exp $
 * WHITEHEAD INSTITUTE
 * SOFTWARE COPYRIGHT NOTICE AGREEMENT
 * This software and its documentation are copyright 2002 by the
 * Whitehead Institute for Biomedical Research.  All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support
 * whatsoever.  The Whitehead Institute can not be responsible for its
 * use, misuse, or functionality.
 */

package edu.mit.wi.pedfile;

/**
 * <p>Title: CheckDataException.java </p>
 * <p>Description: </p>
 * @author Hui Gong
 * @version $Revision 1.1 $
 */

public class CheckDataException extends Exception{

	public CheckDataException() {
		super();
	}

	public CheckDataException(String s){
		super(s);
	}

}
