namespace Revenj.DomainPatterns
{
	public class InvalidItem : IValidationResult
	{
		public string URI { get; set; }
		public string ErrorDescription { get; set; }
	}

	public class InvalidItem<TEntity> : InvalidItem, INestedValue<TEntity>
	{
		public TEntity Value { get; set; }
	}
}
