module Billing
{
	aggregate Invoice
	{
		Item[] Items;
		calculated money Total from 'it => it.Items.Sum(i => i.Price)';
	}
	value Item
	{
		string Product;
		money Price;
	}
	event Invoice.AddItem
	{
		string Product;
		money Price;
	}
}