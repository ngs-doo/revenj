namespace Revenj.Plugins.Server.Commands
{
	public enum GenericSearchFilter
	{
		Equals = 0,
		NotEquals = 1,
		LessThen = 2,
		LessOrEqualThen = 3,
		GreaterThen = 4,
		GreaterThenOrEqual = 5,
		ValueIn = 6,
		ValueNotIn = 7,
		InValue = 8,
		NotInValue = 9,
		StartsWithValue = 10,
		StartsWithCaseInsensitiveValue = 11,
		NotStartsWithValue = 12,
		NotStartsWithCaseInsensitiveValue = 13,
		ValueStartsWith = 14,
		ValueStartsWithCaseInsensitive = 15,
		NotValueStartsWith = 16,
		NotValueStartsWithCaseInsensitive = 17
	}
}
