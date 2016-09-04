using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Runtime.Serialization;

namespace Revenj.DomainPatterns
{
	[Serializable]
	[DataContract(Namespace = "")]
	public class SpecificationByUri : ISpecification<IIdentifiable>
	{
		[DataMember]
		public string[] Uris { get; private set; }

		public SpecificationByUri(IEnumerable<string> uris)
		{
			this.Uris = (uris ?? new string[0]).Where(it => it != null).ToArray();
		}

		public Expression<Func<IIdentifiable, bool>> IsSatisfied
		{
			get { return it => Uris.Contains(it.URI); }
		}
	}
}
