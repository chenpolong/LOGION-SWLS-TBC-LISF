/**
 * Copyright (C) 2006 United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration
 * (NASA).  All Rights Reserved.
 *
 * This software is distributed under the NASA Open Source Agreement
 * (NOSA), version 1.3.  The NOSA has been approved by the Open Source
 * Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
 * directory tree for the complete NOSA document.
 *
 * THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
 * KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
 * LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
 * SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
 * THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
 * DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
 */
package ltlparse;

import gov.nasa.ltl.trans.ParseErrorException;

import java.util.HashSet;
import java.util.Set;

/**
 * Written by Dimitra Giannakopoulou, 19 Jan 2001
 * Parser by Flavio Lerda, 8 Feb 2001
 * Parser extended by Flavio Lerda, 21 Mar 2001
 * Modified to accept && and || by Roby Joehanes 15 Jul 2002
 */
public class Parser {

  /**
   * DOCUMENT ME!
   */
	
  public static Set<String> propositions = new HashSet<>(); //added by RENZO to translate LTL2RE

  private static class Input {
    private StringBuilder sb;

    public Input (String str) {
      sb = new StringBuilder (str);
    }

    public char get () throws EndOfInputException {
      try {
        return sb.charAt (0);
      } catch (StringIndexOutOfBoundsException e) {
        throw new EndOfInputException ();
      }
    }

    public void skip () throws EndOfInputException {
      try {
        sb.deleteCharAt (0);
      } catch (StringIndexOutOfBoundsException e) {
        throw new EndOfInputException ();
      }
    }
  }

  /**
   * DOCUMENT ME!
   */
  @SuppressWarnings ("serial")
  private static class EndOfInputException extends Exception {
  }

  public static Formula<String> parse (String str) throws ParseErrorException { // "aObAc"

    Input i = new Input (str);
//    propositions.clear();//added by RENZO to translate LTL2RE
    return parse (i, P_ALL);
  }

  private static Formula<String> parse (Input i, int precedence) throws ParseErrorException {
    try {
    	
      Formula<String> formula;
      char ch;

      while (i.get () == ' ') {
        i.skip ();
      }

      switch (ch = i.get ()) {
      case '/': // and
      case '&': // robbyjo's and
      case '\\': // or
      case '|': // robbyjo's or
      case 'U': // until
      case 'W': // weak until
      case 'V': // release
      case 'M': // dual of W - weak release
      case ')':
        throw new ParseErrorException ("invalid character: " + ch);

      case '!': // not
        i.skip ();
        formula = Formula.Not (parse (i, P_NOT));

        break;

      case 'X': // next
        i.skip ();
        formula = Formula.Next (parse (i, P_NEXT));

        break;

      case '[': // always
        i.skip ();

        if (i.get () != ']') {
          throw new ParseErrorException ("expected ]");
        }

        i.skip ();
        formula = Formula.Always (parse (i, P_ALWAYS));

        break;

      case '<': // eventually
        i.skip ();

        if (i.get () != '>') {
          throw new ParseErrorException ("expected >");
        }

        i.skip ();
        formula = Formula.Eventually (parse (i, P_EVENTUALLY));

        break;

      case '(':
        i.skip ();
        formula = parse (i, P_ALL);

        if (i.get () != ')') {
          throw new ParseErrorException ("invalid character: " + ch);
        }

        i.skip ();

        break;

      case '"':

        StringBuilder sb = new StringBuilder ();
        i.skip ();

        while ((ch = i.get ()) != '"') {
          sb.append (ch);
          i.skip ();
        }

        i.skip ();

        formula = Formula.Proposition (sb.toString ());
        propositions.add(sb.toString ());//added by RENZO to translate LTL2RE
        break;

      default:

        if (Character.isJavaIdentifierStart (ch)) {
          StringBuilder sbf = new StringBuilder ();

          sbf.append (ch);
          i.skip ();

          try {
            while (Character.isJavaIdentifierPart (ch = i.get ())
                && (!Parser.is_reserved_char (ch))) {
              sbf.append (ch);
              i.skip ();
            }
          } catch (EndOfInputException e) {
            // return Proposition(sbf.toString());
          }

          String id = sbf.toString ();

          if (id.equals ("true")) {
            formula = Formula.True ();
          } else if (id.equals ("false")) {
            formula = Formula.False ();
          } else {
            formula = Formula.Proposition (id);
            propositions.add(id);//added by RENZO to translate LTL2RE
//            System.out.println("new prop: "+id);
          }
        } else {
          throw new ParseErrorException ("invalid character: " + ch);
        }

        break;
      }

      try {
        while (i.get () == ' ') {
          i.skip ();
        }

        ch = i.get ();
      } catch (EndOfInputException e) {
        return formula;
      }

      while (true) {
        switch (ch) {
        case '/': // and

          if (precedence > P_AND) {
            return formula;
          }

          i.skip ();

          if (i.get () != '\\') {
            throw new ParseErrorException ("expected \\");
          }

          i.skip ();
          formula = Formula.And (formula, parse (i, P_AND));

          break;

        case '&': // robbyjo's and

          if (precedence > P_AND) {
            return formula;
          }

          i.skip ();

          if (i.get () != '&') {
            throw new ParseErrorException ("expected &&");
          }

          i.skip ();
          formula = Formula.And (formula, parse (i, P_AND));

          break;

        case '\\': // or

          if (precedence > P_OR) {
            return formula;
          }

          i.skip ();

          if (i.get () != '/') {
            throw new ParseErrorException ("expected /");
          }

          i.skip ();
          formula = Formula.Or (formula, parse (i, P_OR));

          break;

        case '|': // robbyjo's or

          if (precedence > P_OR) {
            return formula;
          }

          i.skip ();

          if (i.get () != '|') {
            throw new ParseErrorException ("expected ||");
          }

          i.skip ();
          formula = Formula.Or (formula, parse (i, P_OR));

          break;

        case 'U': // until

          if (precedence > P_UNTIL) {
            return formula;
          }

          i.skip ();
          formula = Formula.Until (formula, parse (i, P_UNTIL));

          break;

        case 'W': // weak until

          if (precedence > P_WUNTIL) {
            return formula;
          }

          i.skip ();
          formula = Formula.WUntil (formula, parse (i, P_WUNTIL));

          break;

        case 'V': // release

          if (precedence > P_RELEASE) {
            return formula;
          }

          i.skip ();
          formula = Formula.Release (formula, parse (i, P_RELEASE));

          break;

        case 'M': // weak_release

          if (precedence > P_WRELEASE) {
            return formula;
          }

          i.skip ();
          formula = Formula.WRelease (formula, parse (i, P_WRELEASE));

          break;

        case '-': // implies

          if (precedence > P_IMPLIES) {
            return formula;
          }

          i.skip ();

          if (i.get () != '>') {
            throw new ParseErrorException ("expected >");
          }

          i.skip ();
          formula = Formula.Implies (formula, parse (i, P_IMPLIES));

          break;
        case '<':
        	if (precedence > P_IFF) {
                return formula;
              }

              i.skip ();
              if (i.get () != '-') {
                  throw new ParseErrorException ("expected -");
                }

                i.skip ();
                
              if (i.get () != '>') {
                throw new ParseErrorException ("expected >");
              }

              i.skip ();
              formula = Formula.Iff (formula, parse (i, P_IFF));

              break;

        case ')':
          return formula;

        case '!':
        case 'X':
        case '[':
        case '(':
        default:
          System.out.println(formula);
          throw new ParseErrorException ("invalid character: " + ch);
        }

        try {
          while (i.get () == ' ') {
            i.skip ();
          }

          ch = i.get ();
        } catch (EndOfInputException e) {
          break;
        }
      }

      return formula;
    } catch (EndOfInputException e) {
      throw new ParseErrorException ("unexpected end of input");
    }
  }
  
  private static final int P_ALL = 0;
  private static final int P_IFF = 1;
  private static final int P_IMPLIES = 1;
  private static final int P_OR = 2;
  private static final int P_AND = 3;
  private static final int P_UNTIL = 4;
  private static final int P_WUNTIL = 4;
  private static final int P_RELEASE = 5;
  private static final int P_WRELEASE = 5;
  private static final int P_NOT = 6;
  private static final int P_NEXT = 6;
  private static final int P_ALWAYS = 6;
  private static final int P_EVENTUALLY = 6;

  private static boolean is_reserved_char (char ch) {
    switch (ch) {
    //		case 't':
    //		case 'f':
    case 'U':
    case 'V':
    case 'W':
    case 'M':
    case 'X':
    case ' ':
    case '<':
    case '>':
    case '(':
    case ')':
    case '[':
    case ']':
    case '-':
  
      // ! not allowed by Java identifiers anyway - maybe some above neither?
      return true;
  
    default:
      return false;
    }
  }

}

