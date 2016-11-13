module Finance
{
	root Currency(Code)
	{
		string Code;
		string Name;
		static USD 'USD';
		static EUR 'EUR';
		event ChangeName 'it => it.Name = Name'
		{
			string Name;
		}
	}
}