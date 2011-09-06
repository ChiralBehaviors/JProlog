package jp.ac.kobe_u.cs.prolog.builtin;
import jp.ac.kobe_u.cs.prolog.lang.*;
import java.util.Hashtable;
/**
   <code>hash_get/3</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
public class PRED_hash_get_3 extends Predicate {
    public static SymbolTerm SYM_NIL = SymbolTerm.makeSymbol("[]");
    public Term arg1, arg2, arg3;

    public PRED_hash_get_3(Term a1, Term a2, Term a3, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        arg3 = a3;
        this.cont = cont;
    }

    public PRED_hash_get_3(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        arg3 = args[2];
        this.cont = cont;
    }

    public int arity() { return 3; }

    public String toString() {
        return "hash_get(" + arg1 + "," + arg2 + "," + arg3 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2, a3;
        a1 = arg1;
        a2 = arg2;
        a3 = arg3;

	Object hash = null;

	a1 = a1.dereference();
	if (a1.isVariable()) {
	    throw new PInstantiationException(this, 1);
	} else if (a1.isSymbol()) {
	    if (! engine.getHashManager().containsKey(a1))
		throw new ExistenceException(this, 1, "hash", a1, "");
	    hash = ((JavaObjectTerm) engine.getHashManager().get(a1)).object();
	} else if (a1.isJavaObject()) {
	    hash = ((JavaObjectTerm) a1).object();
	} else {
	    throw new IllegalDomainException(this, 1, "hash_or_alias", a1);
	}
	if (! (hash instanceof HashtableOfTerm))
	    throw new InternalException(this + ": Hash is not HashtableOfTerm");
	a2 = a2.dereference();
	Term elem = ((HashtableOfTerm) hash).get(a2);
	if (elem == null)
	    elem = SYM_NIL;
	if (! a3.unify(elem, engine.trail))
	    return engine.fail();
        return cont;
    }
}
