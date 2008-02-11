/*
 * $Id: CheckDataException.java,v 3.1 2008/02/11 19:21:35 htl10 Exp $
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
    static final long serialVersionUID = 5362642498353473882L;

	public CheckDataException() {
		super();
	}

	public CheckDataException(String s){
		super(s);
	}

}
