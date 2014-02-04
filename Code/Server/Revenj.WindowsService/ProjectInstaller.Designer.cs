namespace Revenj.WindowsService
{
	partial class ProjectInstaller
	{
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.IContainer components = null;

		/// <summary> 
		/// Clean up any resources being used.
		/// </summary>
		/// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
		protected override void Dispose(bool disposing)
		{
			if (disposing && (components != null))
			{
				components.Dispose();
			}
			base.Dispose(disposing);
		}

		#region Component Designer generated code

		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			this.revenjServiceProcessInstaller = new System.ServiceProcess.ServiceProcessInstaller();
			this.revenjServiceInstaller = new System.ServiceProcess.ServiceInstaller();
			// 
			// revenjServiceProcessInstaller
			// 
			this.revenjServiceProcessInstaller.Account = System.ServiceProcess.ServiceAccount.LocalService;
			this.revenjServiceProcessInstaller.Password = null;
			this.revenjServiceProcessInstaller.Username = null;
			// 
			// revenjServiceInstaller
			// 
			this.revenjServiceInstaller.DelayedAutoStart = true;
			this.revenjServiceInstaller.Description = "Revenj windows service. Host for windows service plugins";
			this.revenjServiceInstaller.DisplayName = "Revenj windows service";
			this.revenjServiceInstaller.ServiceName = "RevenjService";
			this.revenjServiceInstaller.StartType = System.ServiceProcess.ServiceStartMode.Automatic;
			// 
			// ProjectInstaller
			// 
			this.Installers.AddRange(new System.Configuration.Install.Installer[] {
			this.revenjServiceProcessInstaller,
			this.revenjServiceInstaller});

		}

		#endregion

		private System.ServiceProcess.ServiceProcessInstaller revenjServiceProcessInstaller;
		private System.ServiceProcess.ServiceInstaller revenjServiceInstaller;
	}
}