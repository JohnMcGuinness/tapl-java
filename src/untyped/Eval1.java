package untyped;

import library.Zero;
import untyped.termalg.external.TermAlgMatcher;
import untyped.termalg.shared.TermAlgQuery;
import utils.ZeroNoRuleApplies;

public interface Eval1<Term> extends TermAlgQuery<Term, Term> {
	untyped.termalg.shared.TermAlg<Term, Term> alg();
	IsVal<Term> isVal();
	TmMap<Term> tmMap();
	TermAlgMatcher<Term, Term> matcher();

	default Zero<Term> m() {
		return new ZeroNoRuleApplies<>();
	}


	default Term termShiftAbove(int d, int c, Term t) {
		return tmMap().visitTerm(t).apply(new TmMapCtx<Term>()
				.setOnVar((c1, x, n) -> x >= c1 ? alg().TmVar(x + d, n + d) : alg().TmVar(x, n + d)).setC(c).setT(t));
	}

	default Term termShift(int d, Term t) {
		return termShiftAbove(d, 0, t);
	}

	default Term termSubst(int j, Term s, Term t) {
		return tmMap().visitTerm(t).apply(new TmMapCtx<Term>()
				.setOnVar((c, x, n) -> x == (j + c) ? termShift(c, s) : alg().TmVar(x, n)).setC(0).setT(t));
	}

	default Term termSubstTop(Term s, Term t) {
		return termShift(-1, termSubst(0, termShift(1, s), t));
	}

	default Term TmApp(Term t1, Term t2) {
		return matcher()
				.TmAbs((x, t) -> isVal().visitTerm(t2) ? termSubstTop(t2, t)
						: (isVal().visitTerm(t1) ? alg().TmApp(t1, visitTerm(t2)) : alg().TmApp(visitTerm(t1), t2)))
				.otherwise(
						() -> isVal().visitTerm(t1) ? alg().TmApp(t1, visitTerm(t2)) : alg().TmApp(visitTerm(t1), t2))
				.visitTerm(t1);
	}
}