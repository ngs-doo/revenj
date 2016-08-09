module test {
	snowflake<Abc> AbcList {
		s;
		ii;
		en;
		en2;
		en3;
		i4;
		another;
		v;
		vv;
		v.x;
		v.f;
		v.bytes;
		v.bb;
		v.d;
		v.dd;
		vvv;
		hasV;
		hasA;
		calculated hasV2 from 'it => it.hasV';
		calculated bool hasA2 from 'it => it.hasA2';
		ent1;
		ent1.i;
		ent2;
		abc1;
		abc1.s s2;
		abc2;
		t;
		tt;
		specification Filter 'it => it.s.StartsWith(m)' { string m; }
	}
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
		timestamp t;
		List<timestamp?>? tt;
		specification Filter 'it => it.t > at' { timestamp at; }
	}
	entity Ent1 { int i; }
	entity Ent2 { float f; }
	entity Ent3(id) { int id; int i; }
	value Val {
		int? x { serialization name xx; }
		float f;
		Set<float?> ff;
		Another a;
		Another? aa;
		Another?[] aaa;
		List<Another> aaaa;
		En en;
		binary bytes;
		List<binary> bb;
		En? en2;
		Linked list<En> en3;
		linked list<int> i4;
		linked list<Another> another;
		date? d;
		List<date> dd;
		calculated hasD from 'it => it.d != null';
		calculated int enSize from 'it => it.en3.Count()';
		specification Filter 'it => it.d == d' { date d; }
	}
	value Another;
	sql AbcSql from '"test"."AbcList"' {
		string s; 
		int[] ii; 
		En en;
		En? en2;
		Linked list<En> en3;
		linked list<int> i4;
		specification Filter 'it => it.s == s' { string s; }
	}
	sql AbcWrite from test.Abc(ID) {
		int ID;
		string s; 
		int[] ii; 
		En en;
		En? en2;
		Linked list<En> en3;
		linked list<int> i4;
		linked list<Another> another;
		Val v;
		Val? vv;
		int[]? iii; 
		int?[] iiii; 
		string? ss; 
		Vector<Val> vvv;
		Set<Another?> a;
		List<string> sss; 
		List<string?>? ssss; 
	}
	struct Struct {
		int i;
		int j;
	}
	event TestMe {
		int x;
		string[] ss;
		Val vv;
		List<Val?>? vvv;
		specification Filter 'it => it.x >= a && it.x <= b' { int a; int b; }
	}
	report ReportMe {
	    int x;
	    Vector<TestMe> events 'it => it.x == x';
	    Abc firstAbc 'it => it.s == "xx"' ORDER BY ID DESC;
	}
}