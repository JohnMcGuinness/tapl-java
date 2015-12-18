package fullsimple;

import fullsimple.tyalg.external.TyAlgMatcher;
import fullsimple.tyalg.shared.GTyAlg;
import utils.ITyEqv;

public interface TyEqv<Ty> extends GTyAlg<Ty, ITyEqv<Ty>>, extension.TyEqv<Ty>, typed.TyEqv<Ty>, variant.TyEqv<Ty> {
	@Override
	TyAlgMatcher<Ty, Boolean> matcher();
}