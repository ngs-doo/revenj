using Revenj.DomainPatterns;
using System;
using tutorial;

namespace AspNetTutorial
{
	internal static class Validate
	{
		public static string Name(ICommand command, string currentValue, string newValue, string path)
		{
			if (currentValue == newValue) return currentValue;
			if (newValue.Length < 2) command.LogError(path, "Name is too short");
			else if (newValue.Length > 100) command.LogError(path, "Name is too long");
			else return newValue;
			return currentValue;
		}

		public static PhoneNumber Phone(ICommand command, PhoneNumber currentValue, PhoneNumber newValue, string path)
		{
			if (Object.Equals(currentValue, newValue)) return currentValue;
			else if (string.IsNullOrEmpty(newValue.regionCode)) command.LogError($"{path}.{nameof(newValue.regionCode)}", "Region code missing");
			else if (!newValue.number.StartsWith('+')) command.LogError($"{path}.{nameof(newValue.number)}", "Phone number must start with +");
			else if (newValue.number.IndexOf('+') != newValue.number.LastIndexOf('+')) command.LogError($"{path}.{nameof(newValue.number)}", "Only one + per number allowed");
			//TODO other validations
			else return newValue;
			return currentValue;
		}
	}
}
