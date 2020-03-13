namespace RobotEngine.Messaging{
	public interface ICommandMessageHandler
	{
		void HandleCommandMessage(CommandMessage cmd);
		
		void Log(string logMessage);
	}
}