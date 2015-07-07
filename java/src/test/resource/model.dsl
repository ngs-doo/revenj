module test {
	value Simple {
		int number;
		text text;
	}
	root Composite(id) {
		uuid id;
		Simple simple;
		List<Entity> entities;
		specification ForSimple 'it => it.simple.number == simple.number' {
			Simple simple;
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

		specification BetweenNumbers 'it => it.number >= min && inSet.Contains(it.number)' {
			decimal min;
			Set<decimal> inSet;
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