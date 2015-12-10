package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import library.Tuple2;
import untyped.PrintBind;

// should be pure
public class Context<Bind> {
	protected List<Tuple2<String, Bind>> binds;
	protected untyped.bindingalg.shared.BindingAlg<Bind, Bind> alg;

	public int length() {
		return binds.size();
	}

	public Context(untyped.bindingalg.shared.BindingAlg<Bind, Bind> alg) {
		this.alg = alg;
		this.binds = new ArrayList<>();
	}

	private Context(untyped.bindingalg.shared.BindingAlg<Bind, Bind> alg, List<Tuple2<String, Bind>> binds) {
		this(alg);
		this.binds = binds;
	}

	private Context<Bind> setBinds(List<Tuple2<String, Bind>> binds) {
		return new Context<>(alg, binds);
	}

	public Context<Bind> addBinding(String name, Bind bind) {
		List<Tuple2<String, Bind>> binds2 = new ArrayList<>(binds);
		binds2.add(0, new Tuple2<>(name, bind));
		return setBinds(binds2);
	}

	public Context<Bind> addName(String name) {
		return addBinding(name, alg.NameBind());
	}

	public boolean isNameBound(String s) {
		return binds.stream().filter(b -> b._1.equals(s)).findFirst().isPresent();
	}

	public Tuple2<Context<Bind>, String> pickFreshName(String n) {
		return isNameBound(n) ? pickFreshName(n + "_") : new Tuple2<>(addBinding(n, alg.NameBind()), n);
	}

	public String index2Name(int i) {
		return binds.get(i)._1;
	}

	public int name2Index(String s) throws Exception {
		return IntStream.range(0, binds.size()).filter(i -> binds.get(i)._1.equals(s)).findFirst()
				.orElseThrow(() -> new Exception("identifier " + s + " is unbound"));
	}

	public Bind getBinding(int i) {
		return binds.get(i)._2;
	}

	public String toString(PrintBind<Bind> printBind) {
		return "{" + binds.stream().map(pr -> "(" + pr._1 + "," + printBind.visitBind(pr._2).apply(this) + ")").collect(Collectors.joining(", ")) + "}";
	}
}