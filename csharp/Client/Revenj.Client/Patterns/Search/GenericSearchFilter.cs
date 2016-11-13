namespace Revenj.DomainPatterns
{
	internal enum GenericSearchFilter
	{
		Equals = 0,
		NotEquals,
		LessThen,
		LessOrEqualThen,
		MoreThen,
		MoreOrEqualThen,
		ValueIn,
		ValueNotIn,
		InValue,
		NotInValue,
		StartsWithValue,
		StartsWithCaseInsensitiveValue,
		NotStartsWithValue,
		NotStartsWithCaseInsensitiveValue,
		ValueStartsWith,
		ValueStartsWithCaseInsensitive,
		ValueNotStartsWith,
		ValueNotStartsWithCaseInsensitive
	}
}
