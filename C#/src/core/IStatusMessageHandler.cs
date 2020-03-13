namespace RobotEngine.Messaging{
	public interface IStatusMessageHandler
	{
		void HandleStatusMessage(StatusMessage cmd);
	}
}