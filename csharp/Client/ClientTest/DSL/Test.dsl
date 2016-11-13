module Test
{
	root Foo (bar)
	{
		string bar;
		int? num;

		specification searchByBar 'it => it.bar.StartsWith(name)'
		{
			string name;
		}
		history;
	}

	root Bar(ID)
	{
		int ID { sequence; }
	}

	root RootWithEntity (name)
	{
		string name;
		EntityTest ent;
		EntityTest1[] entarr;
	}

	entity EntityTest
	{
		string name;
		string[] strArr;
		integer[] intArr;
	}

	entity EntityTest1
	{
		string name;
		ValueTest val;
	}

	value ValueTest
	{
		string name;
	}

	report FooReport
	{
		string uri;
		Foo foo 'it => it.URI == uri';
	}

	snowflake FooGrid from Foo
	{
		bar;
		num;
	}

	cube FooCube from FooGrid
	{
		dimension bar;
		count num as count;
		sum num as total;
		average num as average;

		specification findByBar
			'it => it.bar.StartsWith(query)'
		{
			string query;
		}

		templater CreatePdf 'testolap.docx';
	}

	root File
	{
		binary Content;
	}

	report TestReport {
		string URI;
		Foo foo 'i => i.URI == URI';
		templater CreatePdf 'Test.docx' pdf;
	}
}