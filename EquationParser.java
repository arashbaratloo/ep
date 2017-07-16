import java.util.NoSuchElementException;
import java.util.*;

public class EquationParser {
  public static void main(String... args) {
    Scanner scan = new Scanner(System.in);
    boolean done = false;
    do {
      System.out.print("How would you input the equation [r/i/d]? ");
      String ans = scan.nextLine().replace(" ", "");
      switch (ans) {
        case "r":
        case "rpn": {
          System.out.print("Enter a RPN equation ");
          String input_str = scan.nextLine();
          RPNParser p = new RPNParser(input_str);
          RPN rpn = p.toRPN();
          log("RPN form:" + rpn.toString());
          log("  RPN evaluates to:" + rpn.evaluate());
          Expr expr = rpn.toExpr();
          log("Inline form:" + expr.toString());
          log("  inline evalutes to:" + expr.evaluate());
          break;
        }
        case "i":
        case "inline": {
          System.out.print("Enter an expression ");
          String input_str = scan.nextLine();
          InlineParser p = new InlineParser(input_str);
          Expr e = p.toExpr();
          log("Inline expression:" + e.toString());
          log("  inline evalues to:" + e.evaluate());
          RPN rpn = e.toRPN();
          log("RPN form:" + rpn.toString());
          log("  RPN evalues to:" + rpn.evaluate());
          break;
        }
        case "d":
        case "done":
        case "stop":
        case "finish": {
          done = true;
          break;
        }
        default:
          log("I don't understand your input [" + ans + "]");
      }
    } while (!done);
    log("good bye!");
  } // end main()

  private static void log(Object obj) {
    System.out.println(String.valueOf(obj));
  }

  // ------------------------------------------------------------------
  public static final class RPN {
    Stack<String> tokens;
    RPN() {
      tokens = new Stack<String>();
    }
    public String toString() {
      return this.tokens.toString();
    }
    public void addToken(String s) {
      this.tokens.push(s);
    }
    public void appendRPN(RPN rpn) {
      Stack<String> temp_stack = new Stack<String>();
      while (!rpn.tokens.empty()) {
        temp_stack.push(rpn.tokens.pop());
      }
      while (!temp_stack.empty()) {
        addToken(temp_stack.pop());
      }
    }

    public int evaluate() {
      List<String> tokens = new ArrayList<String>(this.tokens);
      Stack<Integer> stack = new Stack<Integer>();
      for (int i = 0; i < tokens.size(); i++) {
        log("   EXECUTION STACK:" + stack.toString());
        String e = tokens.get(i);
        switch (e) {
          case "*":
            stack.push(stack.pop() * stack.pop());
            break;
          case "/":
            stack.push(stack.pop() / stack.pop());
            break;
          case "+":
            stack.push(stack.pop() + stack.pop());
            break;
          case "-":
            stack.push(stack.pop() - stack.pop());
            break;
          default:
            stack.push(Integer.parseInt(e));
        }
      }
      log("   EXECUTION STACK:" + stack.toString());
      return stack.peek();
    }

    //  Converts to an inline expression and returns it.
    public Expr toExpr() {
      List<String> tokens = new ArrayList<String>(this.tokens);
      Stack<Expr> stack = new Stack<Expr>();
      for (int i = 0; i < tokens.size(); i++) {
        String e = tokens.get(i);
        switch (e) {
          case "*":
          case "/":
          case "+":
          case "-":
            Expr op1 = stack.pop();
            Expr op2 = stack.pop();
            Node node = new Node(e, op1, op2);
            stack.push(node);
            break;
          default:
            stack.push(new Term(e));
        }
      }
      return stack.peek();
    } // end toExpr().
  } // end class RPN

  public static final class RPNParser {
    String [] tokens;
    RPNParser(String input) {
      // this.tokens = new Tokenizer(input).getTokens();
      this.tokens = input.split("\\s+");
    }
    public RPN toRPN() {
      RPN rpn = new RPN();
      for (String token : tokens) {
        rpn.addToken(token);
      }
      return rpn;
    }
  } // end class RPNParser.

  // ------------------------------------------------------------------
  // A binary tree-like data structure for representing an mathematical
  // expression.
  // Expr is a parent class of of all nodes in an expression tree. Each node
  // in an expression tree is either a Node or a Term (both subclasses of Expr).
  // Node contains an operator and two operands (left and right). The operands
  // can be a final value (e.g. integer 5) or another expression tree.
  // Leafs in an expression tree are represented by Term's which are
  // simple numbers.
  interface Expr {
    public int evaluate();
    public String toString();
    public RPN toRPN();
  }

  public static final class Node implements Expr {
    String operator;
    Expr left;
    Expr right;
    Node(String op, Expr l, Expr r) {
      this.operator = op;
      this.left = l;
      this.right = r;
    }
    public int evaluate() {
      switch (this.operator) {
        case "*": return this.left.evaluate() * this.right.evaluate();
        case "/": return this.left.evaluate() / this.right.evaluate();
        case "+": return this.left.evaluate() + this.right.evaluate();
        case "-": return this.left.evaluate() - this.right.evaluate();
        default:
          throw new UnsupportedOperationException();
      }
    }
    public String toString() {
      return "(" + this.left.toString() + this.operator + this.right.toString() + ")";
    }
    public RPN toRPN() {
      RPN rpn = new RPN();
      rpn.appendRPN(this.left.toRPN());
      rpn.appendRPN(this.right.toRPN());
      rpn.addToken(this.operator);
      return rpn;
    }
  }

  public static final class Term implements Expr {
    int val;
    Term(String str) {
      this.val = Integer.parseInt(str);
    }
    public int evaluate() {
      return this.val;
    }
    public String toString() {
      return Integer.toString(this.val);
    }
    public RPN toRPN() {
      RPN rpn = new RPN();
      rpn.addToken(toString());
      return rpn;
    }
  }

  public static final class InlineParser {
    String [] tokens;
    InlineParser(String input) {
      this.tokens = new Tokenizer(input).getTokens();
    }
    public Expr toExpr() {
      return ParseExpr(this.tokens);
    }
    private Expr ParseExpr(String [] tokens) {
      // log("ParseExpr" + Arrays.toString(tokens)); // For debugging
      // Scan from left to right looking for the first + or - that is
      // not inside a paranthesis (i.e. are at the top level).
      // Once such an operator if found, then each side is recursively
      // parsed before creating and returning a Node.
      // If no such operator is found, the code falls to the next for loop.
      int level = 0;
      for (int i = tokens.length-1; i >= 0; i--) {
        String token = tokens[i];
        if (token.equals(")")) {
          level++;
        } else if (token.equals("(")) {
          level--;
        } else if (level > 0) {
          ; // skip until paranthesis are ballanced.
        } else if (token.equals("+") || token.equals("-")) {
          String [] left_tokens = Arrays.copyOfRange(tokens, 0, i);
          String [] right_tokens = Arrays.copyOfRange(tokens, i+1, tokens.length);
          return new Node(token, ParseExpr(left_tokens), ParseExpr(right_tokens));
        }
      }
      // Similar to the above for loop exept looking for first * or /.
      for (int i = tokens.length-1; i >= 0; i--) {
        String token = tokens[i];
        if (token.equals(")")) {
          level++;
        } else if (token.equals("(")) {
          level--;
        }
        if (level > 0) {
          continue;
        }
        if (token.equals("*") || token.equals("/")) {
          String [] left_tokens = Arrays.copyOfRange(tokens, 0, i);
          String [] right_tokens = Arrays.copyOfRange(tokens, i+1, tokens.length);
          return new Node(token, ParseExpr(left_tokens), ParseExpr(right_tokens));
        }
      }
      // At this point, the equation must be either a number or ().

      // Check for the () case -- pull out insid and recursively parse it.
      if (tokens[0].equals("(")) {
        for(int i = 0; i < tokens.length; i++) {
          String token = tokens[i];
          if (token.equals("(")) {
            ++level;
          } else if (token.equals(")")) {
            --level;
          }
          if (level == 0) {
              String [] inside_tokens = Arrays.copyOfRange(tokens, 1, i);
              return ParseExpr(inside_tokens);
          }
        }
      } else {
        return new Term(tokens[0]);
      }
      throw new IllegalArgumentException();
    }
  } // end class InlineParser


  // ------------------------------------------------------------------
  // Utility iterator class to parse a string into its individual tokens.
  // Specifically designed to parse a mathematical equation with +,
  // -, *, / operators, and ().
  public static final class Tokenizer {
    private String [] tokens;

    Tokenizer(String str) {
      // The way the regex works is that it looks for two things
      // (the regex is in two parts - using "lookaround" expressions)....
      // (?=[/*]) - A positive zero-width look-ahead - This says: find any
      // gap between two characters where the next character is a / or *.
      // (?<=[/*]) - A positive zero-width look-behind - This says: *find
      // any gap between two characters where the previous character
      // was a / or *.
      // Put them together with an or condition, it says: split the input
      // on the gaps before and after / or *.
      //
      // Alternatively use java.io.StreamTokenizer class.
      String [] tokens = str.split("(?<=[-+*/()])|(?=[-+*/()])");
      this.tokens = new String[tokens.length];
      for (int i = 0; i < tokens.length; i++) {
        this.tokens[i] = tokens[i].replace(" ", "");
      }
    }

    public String [] getTokens() {
      return this.tokens;
    }
  } // end class Tokenizer
}
