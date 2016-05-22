using System;

namespace Revenj
{
	/// <summary>
	/// Structure for hierarchical modeling.
	/// Supports labels separated by dots. Only alphanumeric values allowed for labels.
	/// eg: Level1.Level2.Level3
	/// </summary>
	public struct TreePath
	{
		/// <summary>
		/// Empty path
		/// </summary>
		public static readonly TreePath Empty = new TreePath(string.Empty, new string[0]);

		private readonly string Value;
		private readonly string[] Parts;

		/// <summary>
		/// Construct path from string value, eg: top.middle.bottom
		/// </summary>
		/// <param name="path">string representation</param>
		public TreePath(string path)
		{
			if (string.IsNullOrEmpty(path))
			{
				Value = string.Empty;
				Parts = Empty.Parts;
			}
			else
			{
				this.Value = path;
				this.Parts = Value.Split('.');
				CheckParts(Parts);
			}
		}
		/// <summary>
		/// Create path from string value, eg: top.middle.bottom
		/// </summary>
		/// <param name="path">string representation</param>
		/// <returns>TreePath instance</returns>
		public static TreePath Create(string path)
		{
			if (string.IsNullOrEmpty(path)) return Empty;
			var parts = path.Split('.');
			CheckParts(parts);
			return new TreePath(path, parts);
		}
		private TreePath(string value, string[] parts)
		{
			this.Value = value;
			this.Parts = parts;
		}

		/// <summary>
		/// Hash code is calculated of string representation
		/// </summary>
		/// <returns>string value hash code</returns>
		public override int GetHashCode() { return Value.GetHashCode(); }
		/// <summary>
		/// Check for equality. Same as checking for string representation equality.
		/// </summary>
		/// <param name="obj">string or TreePath</param>
		/// <returns>is string representation equal</returns>
		public override bool Equals(object obj)
		{
			return obj is TreePath && ((TreePath)obj).Value == this.Value
				|| obj is string && (string)obj == this.Value;
		}
		/// <summary>
		/// Return string representation of the value
		/// </summary>
		/// <returns></returns>
		public override string ToString() { return Value; }

		/// <summary>
		/// Implicit conversion to string. Same as calling .ToString()
		/// </summary>
		/// <param name="path">TreePath structure</param>
		/// <returns>string representation.</returns>
		public static implicit operator string(TreePath path)
		{
			return path.Value;
		}
		/// <summary>
		/// Implicit conversion to TreePath. Same as calling new TreePath(string)
		/// </summary>
		/// <param name="path">string value</param>
		/// <returns>TreePath structure</returns>
		public static implicit operator TreePath(string path)
		{
			return new TreePath(path);
		}

		private static void CheckParts(string[] parts)
		{
			for (int i = 0; i < parts.Length; i++)
			{
				var p = parts[i];
				for (int j = 0; j < p.Length; j++)
				{
					if (!Char.IsLetterOrDigit(p[j]))
						throw new ArgumentException("Invalid value for part: " + p + ". Only [A-Za-z0-9] allowed for labels");
				}
			}
		}

		private static bool Compare(string[] left, string[] right, int count)
		{
			for (int i = 0; i < count; i++)
				if (left[i] != right[i]) return false;
			return true;
		}
		/// <summary>
		/// Concatenate two paths
		/// </summary>
		/// <param name="left">parent path</param>
		/// <param name="right">child path</param>
		/// <returns>combined path</returns>
		public static TreePath operator +(TreePath left, TreePath right)
		{
			if (left.Value.Length == 0) return right;
			else if (right.Value.Length == 0) return left;
			var values = new string[left.Parts.Length + right.Parts.Length];
			Array.Copy(left.Parts, values, left.Parts.Length);
			Array.Copy(right.Parts, 0, values, left.Parts.Length, right.Parts.Length);
			return new TreePath(left.Value + "." + right.Value, values);
		}

		public bool IsAncestor(TreePath other)
		{
			if (this.Value == other.Value) return true;
			return this.Parts.Length < other.Parts.Length && Compare(this.Parts, other.Parts, this.Parts.Length);
		}

		public bool IsDescendant(TreePath other)
		{
			if (this.Value == other.Value) return true;
			return this.Parts.Length > other.Parts.Length && Compare(this.Parts, other.Parts, other.Parts.Length);
		}

		public int Level { get { return Parts.Length; } }
		//TODO: various methods
	}
}
