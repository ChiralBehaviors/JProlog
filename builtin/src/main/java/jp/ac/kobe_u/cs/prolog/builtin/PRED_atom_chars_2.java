package jp.ac.kobe_u.cs.prolog.builtin;
import  jp.ac.kobe_u.cs.prolog.lang.*;
/**
 * <code>atom_chars/2</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.1
 */
public class PRED_atom_chars_2 extends Predicate {
    static SymbolTerm Nil = SymbolTerm.makeSymbol("[]");
    Term arg1, arg2;

    public PRED_atom_chars_2(Term a1, Term a2, Predicate cont) {
	arg1 = a1;
	arg2 = a2;
	this.cont = cont;
    }

    public PRED_atom_chars_2() {}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	arg2 = args[1];
	this.cont = cont;
    }

    public int arity() { return 2 ; }

    public String toString() { return "atom_chars(" + arg1 + "," + arg2 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1, a2;
	a1 = arg1;
	a2 = arg2;

	a1 = a1.dereference();
	a2 = a2.dereference();
	if (a1.isVariable()) { // atom_chars(-Atom, +CharList)
	    if (a2.isNil()) {
		if (! a1.unify(SymbolTerm.makeSymbol(""), engine.trail))
		    return engine.fail();
		return cont;
	    } else if (a2.isVariable()) {
		throw new PInstantiationException(this, 2);
	    } else if (! a2.isList()) {
		throw new IllegalTypeException(this, 2, "list", a2);
	    }
	    StringBuffer sb = new StringBuffer();
	    Term x = a2;
	    while(! x.isNil()) {
		if (x.isVariable())
		    throw new PInstantiationException(this, 2);
		if (! x.isList())
		    throw new IllegalTypeException(this, 2, "list", a2);
		Term car = ((ListTerm)x).car().dereference();
		if (car.isVariable())
		    throw new PInstantiationException(this, 2);
		if (! car.isSymbol() || ((SymbolTerm)car).name().length() != 1)
		    throw new IllegalTypeException(this, 2, "character", a2);
		sb.append(((SymbolTerm)car).name());
		x = ((ListTerm)x).cdr().dereference();
	    }
	    if (! a1.unify(SymbolTerm.makeSymbol(sb.toString()), engine.trail))
		return engine.fail();
	    return cont;
	} else if (a2.isNil() || a2.isVariable() || a2.isList()) { // atom_chars(+Atom, ?CharList)
	    if (! a1.isSymbol())
		throw new IllegalTypeException(this, 1, "atom", a1);
	    String s = ((SymbolTerm)a1).name();
	    Term x = Nil;
	    for (int i=s.length(); i>0; i--) {
		x = new ListTerm(SymbolTerm.makeSymbol(s.substring(i-1,i)), x);
	    }
	    if(! a2.unify(x, engine.trail)) 
		return engine.fail();
	    return cont;
	} else {
	    return engine.fail();
	}
    }
}
