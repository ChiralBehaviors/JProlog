package jp.ac.kobe_u.cs.prolog.builtin;
import jp.ac.kobe_u.cs.prolog.lang.*;
import java.io.*;
/**
 * <code>get_byte/2</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.1
 * @since 1.1
*/
public class PRED_get_byte_2 extends Predicate {
    public static IntegerTerm INT_EOF = new IntegerTerm(-1);
    public Term arg1, arg2;

    public PRED_get_byte_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        this.cont = cont;
    }

    public PRED_get_byte_2(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() {
        return "get_byte(" + arg1 + "," + arg2 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;
	Object stream = null;

	// InByte
	a2 = a2.dereference(); 
	if (! a2.isVariable()) {
	    if (! a2.isInteger())
		throw new IllegalTypeException(this, 2, "in_byte", a2);
	    int n = ((IntegerTerm)a2).intValue();
	    if (n != -1 && (n < 0 || n > 255))
		throw new RepresentationException(this, 2, "in_byte");
	}
	// S_or_a
	a1 = a1.dereference(); 
	if (a1.isVariable()) {
	    throw new PInstantiationException(this, 1);
	} else if (a1.isSymbol()) {
	    if (! engine.getStreamManager().containsKey(a1))
		throw new ExistenceException(this, 1, "stream", a1, "");
	    stream = ((JavaObjectTerm) engine.getStreamManager().get(a1)).object();
	} else if (a1.isJavaObject()) {
	    stream = ((JavaObjectTerm) a1).object();
	} else {
	    throw new IllegalDomainException(this, 1, "stream_or_alias", a1);
	}
	if (! (stream instanceof PushbackReader))
	    throw new PermissionException(this, "input", "stream", a1, "");
	// read single byte
	try {
	    int c = ((PushbackReader)stream).read();
	    if (c < 0) { // EOF
		if (! a2.unify(INT_EOF, engine.trail))
		    return engine.fail();
		return cont;
	    } 
	    if (c > 255)
		throw new RepresentationException(this, 0, "byte");
	    if (! a2.unify(new IntegerTerm(c), engine.trail))
		return engine.fail();
	    return cont;
	} catch (IOException e) {
	    throw new TermException(new JavaObjectTerm(e));
	}
    }
}
