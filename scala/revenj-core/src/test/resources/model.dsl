module test {
	enum En { A; B; C; }
	root Abc { 
		string s; 
		int[] ii; 
		En en;
		En? en2;
		Linked list<En> en3;
		linked list<int> i4;
		linked list<Another> another;
		int[]? iii; 
		int?[] iiii; 
		string? ss; 
		List<string> sss; 
		List<string?>? ssss; 
		Val v;
		Val? vv;
		Vector<Val> vvv;
		Set<Another?> a;
		calculated hasV from 'it => it.vv != null';
		calculated bool hasA from 'it => it.a.Count > 0';
		Ent1 ent1;
		Ent2[] ent2;
		Abc? *abc1;
		Queue<Abc> *abc2;
	}
	entity Ent1 { int i; }
	entity Ent2 { float f; }
	entity Ent3(id) { int id; int i; }
	value Val {
		int? x;
		float f;
		Set<float?> ff;
		Another a;
		Another? aa;
		Another?[] aaa;
		List<Another> aaaa;
		En en;
		En? en2;
		Linked list<En> en3;
		linked list<int> i4;
		linked list<Another> another;
		date? d;
		calculated hasD from 'it => it.d != null';
		calculated int enSize from 'it => it.en3.Count()';
	}
	value Another;
}