module test {
	value Simple {
		int number;
		text text;
	}
	root Composite(id) {
		uuid id;
		Simple simple;
	}
	event Clicked {
		date? date;
		decimal number;
		long? bigint;
		Set<bool> bool;
	}
}