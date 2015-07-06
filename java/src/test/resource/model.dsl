module test {
	value Simple {
		int number;
		text text;
	}
	root Composite(id) {
		uuid id;
		Simple simple;
		List<Entity> entities;
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
	}
}
module Seq {
	root Next;
}