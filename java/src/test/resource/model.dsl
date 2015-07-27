module test {
	value Simple {
		int number;
		text text;
		En? en;
		En en2;
		bool? nb;
	}
	enum En { A; B; }
	root Composite(id) {
		uuid id;
		En[] enn;
		Simple simple;
		List<Entity> entities;
		specification ForSimple 'it => it.simple.number == simple.number' {
			Simple simple;
		}
	}
	snowflake<Composite> CompositeList {
		id;
		enn;
		entities;
		simple;
		specification ForSimple 'it => simples.Contains(it.simple)' {
			List<Simple> simples;
		}
	}
	entity Entity {
		money money;
		string(10) id;
		Composite? *composite;
	}
	event Clicked {
		date? date;
		decimal number;
		long? bigint;
		Set<bool> bool;
		En? en;

		specification BetweenNumbers 'it => it.number >= min && inSet.Contains(it.number) && it.en == en' {
			decimal min;
			Set<decimal> inSet;
			En? en;
		}
	}
}
module Seq {
	root Next {
		specification BetweenIds 'it => min == null || it.ID >= min.Value && it.ID <= max' {
			int? min;
			int max;
		}
	}
}