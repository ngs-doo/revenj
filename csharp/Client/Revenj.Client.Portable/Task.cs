namespace System.Threading.Tasks
{
	public class Task
	{
		private readonly Action DoAction;
		protected bool HasRun;
		public Exception Exception { get; protected set; }

		internal event EventHandler TaskCompleted = (s, ea) => { };

		internal Task(Action action, Task previousTask)
		{
			this.DoAction = action;
			if (previousTask == null || previousTask.IsCompleted)
				ThreadPool.QueueUserWorkItem(Run);
			else
				previousTask.TaskCompleted += (s, ea) => ThreadPool.QueueUserWorkItem(Run);
		}

		public void Wait()
		{
			if (!HasRun) Run(null);
		}

		protected virtual void Run(object o)
		{
			lock (this)
			{
				if (HasRun)
					return;
				HasRun = true;
			}
			try
			{
				DoAction();
			}
			catch (Exception ex)
			{
				Exception = ex;
			}
			finally
			{
				MarkCompleted();
			}
		}

		protected void MarkCompleted()
		{
			IsCompleted = true;
			TaskCompleted(this, EventArgs.Empty);
		}

		public Task ContinueWith(Action<Task> newAction)
		{
			return new Task(() => newAction(this), this);
		}

		public Task<T> ContinueWith<T>(Func<Task, T> newFunc)
		{
			return new Task<T>(() => newFunc(this), this);
		}

		public static class Factory
		{
			public static Task StartNew(Action action)
			{
				return new Task(action, null);
			}
			public static Task<T> StartNew<T>(Func<T> func)
			{
				return new Task<T>(func, null);
			}
		}

		//public object AsyncState { get { return null; } }
		//public WaitHandle AsyncWaitHandle { get { return null; } }
		//public bool CompletedSynchronously { get { return false; } }
		public bool IsCompleted { get; protected set; }
	}

	public class Task<T> : Task
	{
		private T calculatedResult;
		public T Result
		{
			get
			{
				if (!IsCompleted) Run(null);
				return calculatedResult;
			}
		}

		private readonly Func<T> DoFunc;

		internal Task(Func<T> func, Task previousTask)
			: base(() => { }, new Task(null, null))
		{
			this.DoFunc = func;
			if (previousTask == null || previousTask.IsCompleted)
				ThreadPool.QueueUserWorkItem(Run);
			else
				previousTask.TaskCompleted += (s, ea) => ThreadPool.QueueUserWorkItem(Run);
		}

		protected override void Run(object o)
		{
			lock (this)
			{
				if (HasRun)
					return;
				HasRun = true;
			}
			try
			{
				calculatedResult = DoFunc();
			}
			catch (Exception ex)
			{
				Exception = ex;
			}
			finally
			{
				MarkCompleted();
			}
		}

		public Task<T> ContinueWith(Func<Task<T>, T> newFunc)
		{
			return new Task<T>(() => newFunc(this), this);
		}

		public Task ContinueWith(Action<Task<T>> newAction)
		{
			return new Task(() => newAction(this), this);
		}

		public Task<TResult> ContinueWith<TResult>(Func<Task<T>, TResult> newFunc)
		{
			return new Task<TResult>(() => newFunc(this), this);
		}
	}
}