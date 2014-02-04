using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using Castle.DynamicProxy;

namespace NGS.Extensibility
{
	internal class CastleInterceptor : IInterceptor
	{
		private readonly Dictionary<MethodInfo, List<Action<object, object[]>>> BeforeAspects;
		private readonly Dictionary<MethodInfo, List<Func<object, object[], Func<object[], object>, object>>> AroundAspects;
		private readonly Dictionary<MethodInfo, List<Func<object, object[], object, object>>> AfterAspects;

		internal CastleInterceptor(
			Dictionary<MethodInfo, List<Action<object, object[]>>> before,
			Dictionary<MethodInfo, List<Func<object, object[], Func<object[], object>, object>>> around,
			Dictionary<MethodInfo, List<Func<object, object[], object, object>>> after)
		{
			this.BeforeAspects = before;
			this.AroundAspects = around;
			this.AfterAspects = after;
		}

		public void Intercept(IInvocation invocation)
		{
			List<Action<object, object[]>> beforeList;
			List<Func<object, object[], Func<object[], object>, object>> aroundList;
			List<Func<object, object[], object, object>> afterList;

			if (BeforeAspects != null && BeforeAspects.TryGetValue(invocation.Method, out beforeList))
				beforeList.ForEach(it => it(invocation.Proxy, invocation.Arguments));

			if (AroundAspects != null && AroundAspects.TryGetValue(invocation.Method, out aroundList))
				aroundList.ForEach(it => invocation.ReturnValue =
					it(invocation.Proxy, invocation.Arguments, args =>
					{
						if (args != null && !args.SequenceEqual(invocation.Arguments))
							for (int i = 0; i < args.Length; i++)
								invocation.Arguments[i] = args[i];
						invocation.Proceed();
						return invocation.ReturnValue;
					}));
			else
				invocation.Proceed();

			if (AfterAspects != null && AfterAspects.TryGetValue(invocation.Method, out afterList))
				afterList.ForEach(it => invocation.ReturnValue = it(invocation.Proxy, invocation.Arguments, invocation.ReturnValue));
		}
	}
}
