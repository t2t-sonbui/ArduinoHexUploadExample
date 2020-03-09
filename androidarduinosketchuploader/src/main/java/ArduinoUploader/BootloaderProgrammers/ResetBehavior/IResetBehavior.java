package ArduinoUploader.BootloaderProgrammers.ResetBehavior;

import ArduinoUploader.BootloaderProgrammers.*;
import ArduinoUploader.Help.ISerialPortStream;

public interface IResetBehavior
{
	ISerialPortStream Reset(ISerialPortStream serialPort, SerialPortConfig config);
}