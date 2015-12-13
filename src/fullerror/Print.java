package fullerror;

import java.util.function.Function;

import utils.Context;

public interface Print<Term, Ty, Bind> extends fullerror.termalg.shared.TermAlg<Term, Ty, Function<Context<Bind>, String>>, simplebool.Print<Term, Ty, Bind> {
	@Override
	default Function<Context<Bind>, String> TmTry(Term t1, Term t2) {
		return ctx -> "try " + visitTerm(t1).apply(ctx) + " with " + visitTerm(t2).apply(ctx);
	}

	@Override
	default Function<Context<Bind>, String> TmError() {
		return ctx -> "error";
	}
}
