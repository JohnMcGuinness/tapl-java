package simplebool;

import java.util.function.Function;

import simplebool.tyalg.external.TyAlgMatcher;
import simplebool.tyalg.shared.TyAlgQuery;

public interface TyEqv<Ty, Bind> extends utils.TyEqv<Ty, Bind> {
	@Override
	TyEqual<Ty> tyEqual();

	interface TyEqual<Ty> extends TyAlgQuery<Ty, Function<Ty, Boolean>>, utils.TyEqv.TyEqual<Ty> {
		TyAlgMatcher<Ty, Boolean> matcher();

		@Override
		default Function<Ty, Boolean> TyArr(Ty tyT1, Ty tyT2) {
			return ty -> matcher()
					.TyArr(tyS1 -> tyS2 -> visitTy(tyT1).apply(tyS1) && visitTy(tyT2).apply(tyS2))
					.otherwise(() -> false)
					.visitTy(ty);
		}
	}
}
