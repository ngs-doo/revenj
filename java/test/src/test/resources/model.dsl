module test {
	value Simple {
		int number;
		text text;
		En? en;
		En en2;
		bool? nb;
		timestamp ts;
	}
	enum En { A; B; }
	root LazyLoad {
		Composite? *comp;
		SingleDetail? *sd;
	}
	root SingleDetail { 
		detail<LazyLoad.sd> details;
	}
	root Composite(id) {
		uuid id;
		En[] enn;
		En en;
		Simple simple;
		date change { versioning; }
		List<timestamp> tsl;
		List<Entity> entities;
		detail<LazyLoad.comp> lazies;
		calculated int entitiesCount from 'it => it.entities.Count';
		calculated bool[] entityHasMoney from 'it => it.entities.Select(e => e.money > 0).ToArray()';
		long[]? indexes;
		calculated hasEntities from 'it => it.entities.Count > 0';
		CompositeList? *selfReference;
		List<CompositeList> *selfReferences;
		specification ForSimple 'it => it.simple.number == simple.number' {
			Simple simple;
		}
	}
	snowflake<Composite> CompositeList {
		id;
		enn;
		en;
		tsl;
		change;
		entities;
		simple;
		simple.number;
		entitiesCount;
		hasEntities;
		entityHasMoney;
		indexes;
		calculated guid id2 from 'it => it.id';
		specification ForSimple 'it => simples.Contains(it.simple)' {
			List<Simple> simples;
		}
		order by id desc;
	}
	cube<CompositeList> CompositeCube {
		dimension number;
		dimension enn;
		dimension hasEntities;
		dimension simple;
		dimension indexes;
		max change max;
		min change min;
		count enn as count;
		sum hasEntities hasSum;
		avg indexes avgInd;
		specification FilterMax 'it => it.change >= value' {
			date value;
		}
		specification ForSimple 'it => simples.Contains(it.simple)' {
			List<Simple> simples;
		}
	}
	entity Entity {
		money money;
		string(10) id;
		Composite? *composite;
		Set<Detail1> detail1;
		Set<Detail2> detail2;
	}
	entity Detail1 {
		float? f;
		float ff;
	}
	entity Detail2 {
		url? u;
		double[] dd;
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
	sql ClickedList from 'SELECT date, number FROM test."Clicked" GROUP BY date, number' {
		date? date;
		decimal? number;
	}
	sql ClickedSeq test.Clicked(id) {
		long id from _event_id { sequence 'test."Clicked__event_id_seq"'; }
		date? date;
		decimal? number;
		specification OnDateOrNumber 'it => it.date == date || it.number >= number || it.date.Value.Year == number' {
			date date;
			int number;
		}
	}	
	specification<ClickedList> FindAt 'it => it.date == date' {
		date date;
	}
	report FindMany {
		uuid id;
		Set<uuid> ids;
		List<CompositeList> composites 'it => ids.Contains(it.id)' order by id limit 4;
		Composite found 'it => it.id == id';
		calculated int idsCount from 'it => it.ids.Count';
		calculated uuid[] uuids from 'it => it.ids.ToArray()';
		CompositeCube(number, simple, count) cube 'it => true' limit 2;
		ClickedList[] clicked 'it => true';
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
module mixinReference {
	mixin Report {
		Author* author;
	}
	aggregate SpecificReport {
		has mixin Report;
	}
	aggregate Author {
		string name;
		Person person;
		Resident *rezident;
		Child[] children;
	}
	struct Test {
		int x;
		Author author;
	}
	entity Person { 
		date birth; 
		calculated int yearOfBirth from 'it => it.birth.Year';
		calculated int dayOfBirth from 'it => it.birth.Day' { persisted; }
		calculated bornOnOddDay from 'it => it.birth.Day % 2 == 1';
		calculated bornOnEvenDay from 'it => it.birth.Day % 2 == 0' { persisted; }
	}
	entity Resident(id) { uuid id; date birth; }
	entity Child { long version; }

	aggregate UserFilter {
		string name;
	}

	permissions {
		filter UserFilter 'it -> it.getName().equals(org.revenj.security.PermissionManager.boundPrincipal.get().getName())' for RegularUser {
			repository;
		}
	}
}
defaults { external permissions disabled; }
role RegularUser;
module binaries {
	guid root Document {
		string(20) name;
		binary content;
		Array<Boolean>? bools;
		calculated Array<Boolean>? boolsCalc from 'it => it.bools';
	}
	sql WritableDocument binaries.Document(id)
	{
		guid id from ID;
		string name;
	}
	sql ReadOnlyDocument 'SELECT "ID", name from binaries."Document"'
	{
		guid ID;
		string Name from name;
	}
}
module security {
	role Admin;
	mixin IsActive {
		bool deactivated;
		with mixin Document;
	}
	mixin Dummy { with mixin Document; }
	root Document {
		map data;
		static MEANING_OF_LIFE '42';
	}
	permissions {
		filter IsActive 'it -> !it.getDeactivated()' except Admin;
	}
}
module egzotics {
	root pks(id) {
		list<int> id;
		xml? xml;
		s3? s3;
		point[] pp;
		location l;
	}
	value v { int x; }
	root PksV(vv, e, ee) {
		v v;
		v[] vv;
		E e;
		Set<E> ee;
		point p;
		List<Location> ll;
	}
	enum E { A; B; C; }
	root Tree(t1) {
		path t1; 
		path? t2; 
		list<path?>? tt1;
		path[] tt2;
	}	
}
module mixintest {
	aggregate Foo {
		List<Bar> bars;
	}
	entity Bar {
		String name;
		String description;
		Mixin  mixin;
	}
	mixin Mixin;
	value ImplVoid {
		has mixin Mixin;
	}
}
module issues {
	big root DateList {
		List<Timestamp?> list;
	}
	root TimestampPk(ts) {
		Timestamp ts;
		decimal(9) d;
		persistence { history; }
	}
	aggregate SA1Bo {
		SE1Bo sE1Bo;
		SV1Bo sV1Bo;
	}
	entity SE1Bo {
		Boolean p1Bo;
	}
	value SV1Bo {
		Boolean p1Bo;
	}
}
module md {
	root Master {
		detail<Detail.master> details;
	}
	entity Detail(id) {
		guid id;
		int masterId;
		Child1[] children1;
		Child2[] children2;
		Reference1 reference1;
		Reference2 reference2;
		relationship master(masterId) Master;
	}
	entity Child1 { int i; }
	entity Child2 { double d; }
	entity Reference1 { long l; }
	entity Reference2 { decimal x; }
}
module adt {
	mixin Auth { apply on module values; }
	value BasicSecurity {
		string username;
		string password;
	}
	value Token {
		string token;
	}
	value Anonymous;
	value DigestSecurity {
		string username;
		binary passwordHash;
	}
	root User(username) {
		string username;
		Auth authentication;
	}
}
module calc {
	root Info(code) {
		String  code;
		String  name { unique; }
	}

	root Type(suffix) {
		String	suffix;
		String  description;
		xml? xml;
	}

	root Realm(id) {
		Info *info { snapshot; }

		Type(type) *refType;
		String type;

		calculated String id from 'it => it.info.code + it.type' { persistable; } //TODO: bad practice navigating over root
		calculated String name from 'it => it.info.name + " (" + it.refType.description + ")"';
	}
}

module stock {
	big aggregate Article {
		Int         projectID;
		String(10)  sku;
		String(25)  title;
	}

	snowflake<Article> ArticleGrid {
		ID;
		projectID;
		sku;
		title;

		specification filterSearch 'it =>
			it.projectID == projectID && (
				filter == null ||
				filter == "" ||
				it.sku.ToLower().Contains(filter.ToLower()) ||
				it.title.ToLower().Contains(filter.ToLower()))' {
			Int     projectID;
			String? filter;
		}
	}

	aggregate Analysis(projectID, articleID) {
		Int          projectID;
		Long         articleID;
		Relationship reportArticle(articleID) stock.Article;

		String(1)? abc;
		String(1)? xyz;
		String(50)? clazz;
	}

	snowflake<Analysis> AnalysisGrid {
		projectID;
		reportArticle.title;
		reportArticle.sku;
		xyz;
		abc;

		specification filterSearch 'it =>
			it.projectID == projectID && (
				filter == null || filter == "" ||
				it.title.ToLower().Contains(filter.ToLower()) ||
				it.sku.ToLower().Contains(filter.ToLower())) &&
			(abc == null || it.abc == abc) &&
			(xyz == null || it.xyz == xyz) && (
				clazz == null || clazz == "" || 
				it.sku.ToLower().Contains(clazz.ToLower()))' {
			Int         projectID;
			String?     filter;
			String(1)?  abc;
			String(1)?  xyz;
			String(50)? clazz;
		}
	}
}
module xc {
	root SearchByTimestampAndOrderByTimestamp {
	    Timestamp ondate;
	    String marker;
	}
  
	uuid aggregate Partner {
		Partner?     *owner;
		specification findChildren 'it => it.ownerID == ownerID' {
			UUID ownerID;
		}
	}

	event CoverageUpdate {
		String supplierID;
	}
	
	sql CoverageVersions1 <# 
		SELECT  _event_id "eventID", "supplierID", _processed_at "processedAt" 
		from xc."CoverageUpdate" 
	#> {
		String supplierID;
		Timestamp processedAt;
	}
}
module events {
	root Root {
		Event? *event;
	}
	event Event {
		Root? *root;
		Event? *event;
	}
	mixin AlertScenario {
		Int partnerID;
	}
	value Alert1 {
		has mixin AlertScenario;
		String prop1;
	}
	value Alert2 {
		has mixin AlertScenario;
		String prop2;
	}
	event AlertEventLog {
		AlertScenario alert;
	}
}
module sql {
	SQL GuardCheck <# SELECT 1.0 as a #> {
		Decimal(5) a;
	}
}
module numbers {
	aggregate LongNumbers {
		EntityWithLongs entity;
	}
	entity EntityWithLongs {
		Array<Long> prop;
	}
	aggregate DecimalPk(d) {
		decimal(4) d;
	}
}
