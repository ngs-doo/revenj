module test {
	snowflake<Abc> AbcList {
		s;
		ii;
		ll;
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
	cube<AbcList> AbcCube {
		dimension s;
		count ii;
		count en as entC;
		count en2;
		sum i;
	}
	enum En { A; B; C { description 'something important'; } }
	root Abc { 
		string s; 
		int[] ii;
		long[] ll;
		En en;
		Set<long>? lll;
		List<long?> llll;
		Set<long?>? lllll;
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
	entity Ent2 { float f; Ent4[] ee; }
	entity Ent3(id) { int id; int i; }
	entity Ent4;
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
	struct Struct { int i { serialization name ii; } array<float> af; Another a; Set<Val> vals {serialization name v; } }
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
		int ID { sequence 'test."Abc_ID_seq"'; } 
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
	root ComplexPk(a,b,c) {
		int a;
		string b;
		decimal?[] c;
		point? p;
		location? l;
		List<point> p2;
		Set<location?> l2;
	}
	root bpk(b) {
		bool?[] b;
	}	
	sql Aliases from '"test"."Abc"' {
		string ssX from s;
	}
	cube<Aliases> A2 {
		dimension ssX;
	}
	aggregate Client(id) {
		Long id;
		int points;
	}
	aggregate CorporateClient(clientId) {
		Long clientId;
		Client(clientId) *baseClient;
	}
	event<Client> Tick {
		int num;
		async;
	}
}