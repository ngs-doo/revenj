using System;
// Copyright (c) rubicon IT GmbH, www.rubicon.eu
//
// See the NOTICE file distributed with this work for additional information
// regarding copyright ownership.  rubicon licenses this file to you under 
// the Apache License, Version 2.0 (the "License"); you may not use this 
// file except in compliance with the License.  You may obtain a copy of the 
// License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the 
// License for the specific language governing permissions and limitations
// under the License.
// 
using System.Collections.Generic;
using System.Linq.Expressions;
using System.Reflection;
using System.Threading;

namespace Remotion.Linq.Parsing.ExpressionTreeVisitors.TreeEvaluation
{
	public class PartialEvaluationInfo
	{
		private readonly HashSet<Expression> _evaluatableExpressions = new HashSet<Expression>();

		public int Count
		{
			get { return _evaluatableExpressions.Count; }
		}

		private static readonly MemberInfo Now = typeof(DateTime).GetMember("Now", BindingFlags.Static | BindingFlags.Public)[0];
		private static readonly MemberInfo UtcNow = typeof(DateTime).GetMember("UtcNow", BindingFlags.Static | BindingFlags.Public)[0];
		private static readonly MemberInfo Today = typeof(DateTime).GetMember("Today", BindingFlags.Static | BindingFlags.Public)[0];
		private static readonly MemberInfo Principal = typeof(Thread).GetMember("CurrentPrincipal", BindingFlags.Static | BindingFlags.Public)[0];

		public bool AddEvaluatableExpression(Expression expression)
		{
			var me = expression as MemberExpression;
			if (me != null && me.NodeType == ExpressionType.MemberAccess)
			{
				if (me.Member == Now || me.Member == UtcNow || me.Member == Today || me.Member == Principal)
					return false;
			}
			_evaluatableExpressions.Add(expression);
			return true;
		}

		public bool IsEvaluatableExpression(Expression expression)
		{
			return _evaluatableExpressions.Contains(expression);
		}
	}
}
