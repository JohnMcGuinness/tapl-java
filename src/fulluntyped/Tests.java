package fulluntyped;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

import org.junit.Test;

import fulluntyped.bindingalg.external.BindVisitor;
import fulluntyped.bindingalg.external.BindingAlgFactory;
import fulluntyped.bindingalg.external.BindingAlgMatcher;
import fulluntyped.bindingalg.external.BindingAlgMatcherImpl;
import fulluntyped.bindingalg.external.IBind;
import fulluntyped.termalg.external.ITerm;
import fulluntyped.termalg.external.TermAlgFactory;
import fulluntyped.termalg.external.TermAlgMatcher;
import fulluntyped.termalg.external.TermAlgMatcherImpl;
import fulluntyped.termalg.external.TermVisitor;
import fulluntyped.termalg.shared.TermAlg;
import library.Tuple2;
import utils.Context;
import utils.Eval;
import utils.varappalg.shared.VarAppAlg;

public class Tests {
	// compiler bug? should not have conflicts
	class PrintImpl implements Print<ITerm, IBind<ITerm>>, TermVisitor<Function<Context<IBind<ITerm>>, String>> {
		public TermAlgMatcher<ITerm, String> matcher() {
			return new TermAlgMatcherImpl<>();
		}

		@Override
		public Function<Context<IBind<ITerm>>, String> visitTerm(ITerm e) {
			return TermVisitor.super.visitTerm(e);
		}

		@Override
		public PrintBind<IBind<ITerm>, ITerm> printBind() {
			return new PrintBindImpl();
		}
	}

	class PrintBindImpl implements PrintBind<IBind<ITerm>, ITerm>,
			BindVisitor<Function<Context<IBind<ITerm>>, String>, ITerm> {
		@Override
		public Print<ITerm, IBind<ITerm>> printTerm() {
			return new PrintImpl();
		}
	}

	class IsNumericValImpl implements IsNumericVal<ITerm>, TermVisitor<Boolean> {
	}

	class IsValImpl implements fulluntyped.IsVal<ITerm>, TermVisitor<Boolean> {
	}

	class TermShiftAndSubstImpl implements TermShiftAndSubst<ITerm> {
		class TmMapImpl implements TmMap<ITerm>,
				TermVisitor<Function<Tuple2<TmMap.VarMapper<ITerm>, Integer>, ITerm>> {
			public TermAlg<ITerm, ITerm> alg() {
				return fact;
			}
		}

		public TmMap<ITerm> tmMap() {
			return new TmMapImpl();
		}

		@Override
		public VarAppAlg<ITerm, ITerm> alg() {
			return fact;
		}
	}

	abstract class Eval1Impl implements Eval1<ITerm, IBind<ITerm>>,
			TermVisitor<ITerm> {

		@Override
		public TermShiftAndSubst<ITerm> termShiftAndSubst() {
			return new TermShiftAndSubstImpl();
		}

		@Override
		public IsNumericVal<ITerm> isNumericVal() {
			return new IsNumericValImpl();
		}

		@Override
		public IsVal<ITerm> isVal() {
			return isVal;
		}

		@Override
		public BindingAlgMatcher<IBind<ITerm>, ITerm, ITerm> bindMatcher() {
			return new BindingAlgMatcherImpl<>();
		}

		@Override
		public TermAlg<ITerm, ITerm> alg() {
			return fact;
		}

		public TermAlgMatcher<ITerm, ITerm> matcher() {
			return new TermAlgMatcherImpl<>();
		}
	}

	abstract class EvalImpl implements Eval<ITerm> {
		@Override
		public boolean isVal(ITerm t) {
			return t.accept(isVal);
		}
	}

	EvalImpl evalCtx(Context<IBind<ITerm>> ctx) {
		return new EvalImpl() {
			@Override
			public ITerm eval1(ITerm t) {
				return t.accept(new Eval1Impl() {
					@Override
					public Context<IBind<ITerm>> ctx() {
						return ctx;
					}
				});
			}
		};
	}

	TermShiftAndSubstImpl termShiftAndSubst = new TermShiftAndSubstImpl();
	TermAlgFactory fact = new TermAlgFactory();
	BindingAlgFactory<ITerm> bindFact = new BindingAlgFactory<>();
	Context<IBind<ITerm>> ctx = new Context<>(new BindingAlgFactory<>());
	PrintImpl print = new PrintImpl();
	PrintBindImpl printBind = new PrintBindImpl();
	IsValImpl isVal = new IsValImpl();
	EvalImpl eval = evalCtx(ctx);

	ITerm t = fact.TmTrue();
	ITerm f = fact.TmFalse();
	ITerm if_f_then_t_else_f = fact.TmIf(f, t, f);
	ITerm x = fact.TmVar(0, 1);
	ITerm id = fact.TmAbs("x", x);
	ITerm lam_x_xx = fact.TmAbs("x", fact.TmApp(x, x));
	ITerm id_lam_x_xx = fact.TmApp(id, fact.TmAbs("x", fact.TmApp(x, x)));
	ITerm record = fact.TmRecord(Arrays.asList(new Tuple2<>("x", id), new Tuple2<>("y", id_lam_x_xx)));
	ITerm proj = fact.TmProj(record, "x");
	ITerm hello = fact.TmString("hello");
	ITerm timesfloat = fact.TmTimesFloat(fact.TmTimesFloat(fact.TmFloat(2f), fact.TmFloat(3f)),
			fact.TmTimesFloat(fact.TmFloat(4f), fact.TmFloat(5f)));
	ITerm o = fact.TmZero();
	ITerm succ_pred_0 = fact.TmSucc(fact.TmPred(o));
	ITerm let_x_t_in_x = fact.TmLet("x", t, x);
	ITerm mixed = fact.TmLet("t", fact.TmApp(proj, t), fact.TmIf(fact.TmVar(0, 1), o, succ_pred_0));

	Context<IBind<ITerm>> ctx2 = ctx.addBinding("x", bindFact.TmAbbBind(t)).addName("y");

	@Test
	public void testPrint() {
		assertEquals("true", t.accept(print).apply(ctx));
		assertEquals("if false then true else false", if_f_then_t_else_f.accept(print).apply(ctx));
		assertEquals("x", x.accept(print).apply(ctx.addName("x")));
		assertEquals("\\x.x", id.accept(print).apply(ctx));
		assertEquals("\\x.x \\x.x x", id_lam_x_xx.accept(print).apply(ctx));
		assertEquals("{x=\\x.x,y=\\x.x \\x.x x}", record.accept(print).apply(ctx));
		assertEquals("{x=\\x.x,y=\\x.x \\x.x x}.x", proj.accept(print).apply(ctx));
		assertEquals("hello", hello.accept(print).apply(ctx));
		assertEquals("timesfloat timesfloat 2.0 3.0 timesfloat 4.0 5.0", timesfloat.accept(print).apply(ctx));
		assertEquals("0", o.accept(print).apply(ctx));
		assertEquals("(succ (pred 0))", succ_pred_0.accept(print).apply(ctx));
		assertEquals("let x=true in x", let_x_t_in_x.accept(print).apply(ctx));
		assertEquals("let t={x=\\x.x,y=\\x.x \\x.x x}.x true in if t then 0 else (succ (pred 0))", mixed.accept(print).apply(ctx));
	}

	@Test
	public void testIsVal() {
		assertTrue(fact.TmRecord(Collections.emptyList()).accept(isVal));
		assertTrue(fact.TmRecord(Collections.singletonList(new Tuple2<>("x", id))).accept(isVal));
		assertFalse(fact.TmRecord(Collections.singletonList(new Tuple2<>("x", id_lam_x_xx))).accept(isVal));
		assertFalse(record.accept(isVal));
		assertTrue(fact.TmRecord(Collections.singletonList(new Tuple2<>("x", t))).accept(isVal));
		assertTrue(fact.TmRecord(Arrays.asList(new Tuple2<>("x", t), new Tuple2<>("y", f))).accept(isVal));
	}

	@Test
	public void testEval() {
		assertEquals("true", eval.eval(t).accept(print).apply(ctx));
		assertEquals("false", eval.eval(if_f_then_t_else_f).accept(print).apply(ctx));
		assertEquals("true", evalCtx(ctx.addBinding("x", bindFact.TmAbbBind(t))).eval(x).accept(print).apply(ctx));
		assertEquals("\\x.x", eval.eval(id).accept(print).apply(ctx));
		assertEquals("0", eval.eval(fact.TmApp(id, o)).accept(print).apply(ctx));
		assertEquals("\\x.x x", eval.eval(id_lam_x_xx).accept(print).apply(ctx));
		assertEquals("{x=\\x.x,y=\\x.x x}", eval.eval(record).accept(print).apply(ctx));
		assertEquals("\\x.x", eval.eval(proj).accept(print).apply(ctx));
		assertEquals("hello", eval.eval(hello).accept(print).apply(ctx));
		assertEquals("0.0", eval.eval(fact.TmFloat(0l)).accept(print).apply(ctx));
		assertEquals("120.0", eval.eval(timesfloat).accept(print).apply(ctx));
		assertEquals("0", eval.eval(o).accept(print).apply(ctx));
		assertEquals("1", eval.eval(succ_pred_0).accept(print).apply(ctx));
		assertEquals("true", eval.eval(let_x_t_in_x).accept(print).apply(ctx));
		assertEquals("0", eval.eval(mixed).accept(print).apply(ctx));
	}

	@Test
	public void testContext() throws Exception {
		assertEquals("{}", ctx.toString(bind -> bind.accept(printBind).apply(ctx)));
		assertEquals("{(x,)}", ctx.addName("x").toString(bind -> bind.accept(printBind).apply(ctx)));
		assertEquals("{(x,true)}", ctx.addBinding("x", bindFact.TmAbbBind(t)).toString(bind -> bind.accept(printBind).apply(ctx)));
		assertEquals("{(y,), (x,true)}", ctx2.toString(bind -> bind.accept(printBind).apply(ctx)));
		assertEquals("y", ctx2.index2Name(0));
		assertEquals("x", ctx2.index2Name(1));
		assertEquals(0, ctx2.name2Index("y"));
		assertEquals(1, ctx2.name2Index("x"));
		assertTrue(ctx2.isNameBound("y"));
		assertTrue(ctx2.isNameBound("x"));
		assertFalse(ctx2.isNameBound("z"));
		assertEquals("x_", ctx2.pickFreshName("x")._2);
		assertEquals(3, ctx2.pickFreshName("x")._1.length());
		assertEquals("z", ctx2.pickFreshName("z")._2);
		assertEquals(3, ctx2.pickFreshName("z")._1.length());
	}

	@Test
	public void testShift() {
		ITerm x2 = fact.TmVar(1, 2);
		ITerm y = fact.TmVar(0, 2);
		assertEquals("if x then y else if y then x else x", termShiftAndSubst.termShift(0, fact.TmIf(x2, y, fact.TmIf(y, x2, x2))).accept(print).apply(ctx2));
		assertEquals("x", termShiftAndSubst.termShift(1, x).accept(print).apply(ctx2));

		// (\.\.1 (0 2)) -> (\.\.1 (0 4))
		ITerm e = fact.TmAbs("x", fact.TmAbs("y", fact.TmApp(fact.TmVar(1, 3), fact.TmApp(fact.TmVar(0, 3), fact.TmVar(2, 3)))));
		assertEquals("\\x.\\y.[bad index: 1/5 in {(y,), (x,)}] [bad index: 0/5 in {(y,), (x,)}] [bad index: 4/5 in {(y,), (x,)}]", termShiftAndSubst.termShift(2, e).accept(print).apply(ctx));
	}

	// Exercise 6.2.5
	@Test
	public void testTermSubst() {
		ITerm e;

		e = fact.TmApp(fact.TmVar(0, 2), fact.TmVar(0, 2));
		assertEquals("b b", e.accept(print).apply(ctx.addName("a").addName("b")));
		assertEquals("a a", termShiftAndSubst.termSubst(0, fact.TmVar(1, 2), e).accept(print).apply(ctx.addName("a").addName("b")));

		e = fact.TmApp(fact.TmVar(0, 2), fact.TmAbs("x", fact.TmAbs("y", fact.TmVar(2, 4))));
		// [b -> a] (b \.x\.y b) = a (\.x\.y a)
		assertEquals("b \\x.\\y.b", e.accept(print).apply(ctx.addName("a").addName("b")));
		assertEquals("a \\x.\\y.a", termShiftAndSubst.termSubst(0, fact.TmVar(1, 2), e).accept(print).apply(ctx.addName("a").addName("b")));

		// [b -> a (\z.a)] (b (\x.b)) = (a (\z.a)) (\x.(a (\z.a)))
		e = fact.TmApp(fact.TmVar(0, 2), fact.TmAbs("x", fact.TmVar(1, 3)));
		assertEquals("b \\x.b", e.accept(print).apply(ctx.addName("a").addName("b")));
		assertEquals("a \\z.a \\x.a \\z.a", termShiftAndSubst.termSubst(0, fact.TmApp(fact.TmVar(1, 2), fact.TmAbs("z", fact.TmVar(2, 3))), e).accept(print).apply(ctx.addName("a").addName("b")));

		// [b -> a] (\b. b a) = (\.b b a)
		assertEquals("\\b.b a", fact.TmAbs("b", fact.TmApp(fact.TmVar(0, 2), fact.TmVar(1, 2))).accept(print).apply(ctx.addName("a")));
		assertEquals("\\b_.b_ a", termShiftAndSubst.termSubst(0, fact.TmVar(1, 2), fact.TmAbs("b", fact.TmApp(fact.TmVar(0, 3), fact.TmVar(2, 3)))).accept(print).apply(ctx.addName("a").addName("b")));

		// [b -> a] (\a. b a) = (\a_. a a_)
		assertEquals("\\a.b a", fact.TmAbs("a", fact.TmApp(fact.TmVar(1, 2), fact.TmVar(0, 2))).accept(print).apply(ctx.addName("b")));
		assertEquals("\\a_.a a_", termShiftAndSubst.termSubst(0, fact.TmVar(1, 2), fact.TmAbs("a", fact.TmApp(fact.TmVar(1, 3), fact.TmVar(0, 3)))).accept(print).apply(ctx.addName("a").addName("b")));
	}
}