package fullsimple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Test;

import fullsimple.bindingalg.external.Bind;
import fullsimple.bindingalg.external.BindVisitor;
import fullsimple.bindingalg.external.BindingAlgFactory;
import fullsimple.bindingalg.shared.GBindingAlg;
import fullsimple.termalg.external.Term;
import fullsimple.termalg.external.TermAlgFactory;
import fullsimple.termalg.external.TermAlgMatcherImpl;
import fullsimple.termalg.external.TermVisitor;
import fullsimple.tyalg.external.Ty;
import fullsimple.tyalg.external.TyAlgFactory;
import fullsimple.tyalg.external.TyAlgMatcher;
import fullsimple.tyalg.external.TyAlgMatcherImpl;
import fullsimple.tyalg.external.TyVisitor;
import fullsimple.tyalg.shared.GTyAlg;
import library.Tuple2;
import library.Tuple3;
import utils.Context;
import utils.ITyEqv;

public class Tests {
	class PrintImpl implements Print<Term<Ty>, Ty, Bind<Term<Ty>, Ty>>,
			TermVisitor<Function<Context<Bind<Term<Ty>, Ty>>, String>, Ty> {

		@Override
		public PrintBind<Bind<Term<Ty>, Ty>, Term<Ty>, Ty> printBind() {
			return printBind;
		}

		@Override
		public PrintTy<Ty, Bind<Term<Ty>, Ty>> printTy() {
			return printTy;
		}

		@Override
		public fullsimple.termalg.external.TermAlgMatcher<Term<Ty>, Ty, String> matcher() {
			return new TermAlgMatcherImpl<>();
		}
	}

	class PrintTyImpl implements PrintTy<Ty, Bind<Term<Ty>, Ty>>,
			TyVisitor<Function<Context<Bind<Term<Ty>, Ty>>, String>> {
	}

	class PrintBindImpl implements PrintBind<Bind<Term<Ty>, Ty>, Term<Ty>, Ty>,
			BindVisitor<Function<Context<Bind<Term<Ty>, Ty>>, String>, Term<Ty>, Ty> {

		@Override
		public PrintTy<Ty, Bind<Term<Ty>, Ty>> printTy() {
			return printTy;
		}

		@Override
		public Print<Term<Ty>, Ty, Bind<Term<Ty>, Ty>> printTerm() {
			return printTerm;
		}
	}

	class GetTypeFromBindImpl
			implements GetTypeFromBind<Bind<Term<Ty>, Ty>, Term<Ty>, Ty>, BindVisitor<Ty, Term<Ty>, Ty> {
	}

	class TyEqvImpl implements TyEqv<Ty>, TyVisitor<ITyEqv<Ty>> {
		@Override
		public TyAlgMatcher<Ty, Boolean> matcher() {
			return new TyAlgMatcherImpl<>();
		}
	}

	class TypeofImpl implements Typeof<Term<Ty>, Ty, Bind<Term<Ty>, Ty>>,
			TermVisitor<Function<Context<Bind<Term<Ty>, Ty>>, Ty>, Ty> {
		@Override
		public GetTypeFromBind<Bind<Term<Ty>, Ty>, Term<Ty>, Ty> getTypeFromBind() {
			return new GetTypeFromBindImpl();
		}

		@Override
		public TyEqv<Ty> tyEqv() {
			return new TyEqvImpl();
		}

		@Override
		public TyAlgMatcher<Ty, Ty> tyMatcher() {
			return new TyAlgMatcherImpl<>();
		}

		@Override
		public GTyAlg<Ty, Ty> tyAlg() {
			return tyFact;
		}

		@Override
		public GBindingAlg<Bind<Term<Ty>, Ty>, Term<Ty>, Ty, Bind<Term<Ty>, Ty>> bindAlg() {
			return bindFact;
		}
	}

	// printers
	PrintTyImpl printTy = new PrintTyImpl();
	PrintImpl printTerm = new PrintImpl();
	PrintBindImpl printBind = new PrintBindImpl();

	// elements
	Ty ty;
	Bind<Term<Ty>, Ty> bind;
	Term<Ty> term;

	// factories
	TyAlgFactory tyFact = new TyAlgFactory();
	BindingAlgFactory<Term<Ty>, Ty> bindFact = new BindingAlgFactory<>();
	TermAlgFactory<Ty> termFact = new TermAlgFactory<>();

	Context<Bind<Term<Ty>, Ty>> ctx = new Context<Bind<Term<Ty>, Ty>>(bindFact);

	Ty bool = tyFact.TyBool();
	Term<Ty> t = termFact.TmTrue();
	Term<Ty> x = termFact.TmVar(0, 1);

	// typer
	TypeofImpl typeof = new TypeofImpl();
	TyEqvImpl tyEqual = new TyEqvImpl();


	@Test
	public void testPrintTyFloat() {
		ty = tyFact.TyFloat();
		assertEquals("Float", ty.accept(printTy).apply(ctx));
	}

	@Test
	public void testPrintTyUnit() {
		ty = tyFact.TyUnit();
		assertEquals("Unit", ty.accept(printTy).apply(ctx));
	}

	@Test
	public void testPrintTyRecord() {
		ty = tyFact.TyRecord(Arrays.asList(new Tuple2<>("bool", tyFact.TyBool()), new Tuple2<>("nat", tyFact.TyNat())));
		assertEquals("{bool:Bool,nat:Nat}", ty.accept(printTy).apply(ctx));
	}

	@Test
	public void testPrintTyVariant() {
		ty = tyFact
				.TyVariant(Arrays.asList(new Tuple2<>("bool", tyFact.TyBool()), new Tuple2<>("nat", tyFact.TyNat())));
		assertEquals("<bool:Bool,nat:Nat>", ty.accept(printTy).apply(ctx));
	}

	@Test
	public void testPrintTyArr() {
		ty = tyFact.TyArr(tyFact.TyString(), tyFact.TyArr(tyFact.TyNat(), tyFact.TyBool()));
		assertEquals("(String -> (Nat -> Bool))", ty.accept(printTy).apply(ctx));
	}

	@Test
	public void testPrintVarBind() {
		bind = bindFact.VarBind(bool);
		assertEquals(": Bool", bind.accept(printBind).apply(ctx));
	}

	@Test
	public void testPrintTyAbbBind() {
		bind = bindFact.TyAbbBind(bool);
		assertEquals("= Bool", bind.accept(printBind).apply(ctx));
	}

	@Test
	public void testPrintTmAbbBind() {
		bind = bindFact.TmAbbBind(t, Optional.of(tyFact.TyBool()));
		assertEquals("= true: Bool", bind.accept(printBind).apply(ctx));
		bind = bindFact.TmAbbBind(t, Optional.empty());
		assertEquals("= true", bind.accept(printBind).apply(ctx));
	}

	@Test
	public void testPrintTmUnit() {
		term = termFact.TmUnit();
		assertEquals("Unit", term.accept(printTerm).apply(ctx));
	}

	@Test
	public void testPrintTmInert() {
		term = termFact.TmInert(bool);
		assertEquals("inert[Bool]", term.accept(printTerm).apply(ctx));
	}

	@Test
	public void testPrintTmFix() {
		term = termFact.TmFix(t);
		assertEquals("fix true", term.accept(printTerm).apply(ctx));
	}

	@Test
	public void testPrintTmTag() {
		term = termFact.TmTag("x", t, bool);
		assertEquals("<x=true> as Bool", term.accept(printTerm).apply(ctx));
	}

	@Test
	public void testPrintTmCase() {
		term = termFact.TmCase(t, Arrays.asList(new Tuple3<>("X", "x", termFact.TmVar(0, 2)),
				new Tuple3<>("Y", "y", termFact.TmVar(0, 2))));
		assertEquals("case true of <X=x>==>x| <Y=y_>==>y_", term.accept(printTerm).apply(ctx.addName("y")));
	}

	@Test
	public void testPrintLet() {
		term = termFact.TmLet("x", t, x);
		assertEquals("let x=true in x", term.accept(printTerm).apply(ctx));
	}

	@Test
	public void testTypeofTmTrue() {
		ty = t.accept(typeof).apply(ctx);
		assertTrue(bool.accept(tyEqual).tyEqv(ty));
	}

	@Test
	public void testTypeofTmAscribe() {
		ty = tyFact.TyUnit();
		term = termFact.TmAscribe(termFact.TmUnit(), ty);
		assertTrue(ty.accept(tyEqual).tyEqv(term.accept(typeof).apply(ctx)));
		assertFalse(bool.accept(tyEqual).tyEqv(term.accept(typeof).apply(ctx)));
	}

	@Test
	public void testTypeofTmTag() {
		ty = tyFact.TyUnit();
		term = termFact.TmAscribe(termFact.TmUnit(), ty);
		assertTrue(ty.accept(tyEqual).tyEqv(term.accept(typeof).apply(ctx)));
		assertFalse(bool.accept(tyEqual).tyEqv(term.accept(typeof).apply(ctx)));
	}
}