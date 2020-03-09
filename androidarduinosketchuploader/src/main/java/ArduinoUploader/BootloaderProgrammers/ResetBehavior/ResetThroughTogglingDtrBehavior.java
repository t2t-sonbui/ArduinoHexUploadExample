package ArduinoUploader.BootloaderProgrammers.ResetBehavior;



import ArduinoUploader.BootloaderProgrammers.*;
import ArduinoUploader.Help.ISerialPortStream;

public class ResetThroughTogglingDtrBehavior implements IResetBehavior {
	private boolean Toggle;

	private boolean getToggle() {
		return Toggle;
	}

	public ResetThroughTogglingDtrBehavior(boolean toggle) {
		Toggle = toggle;
	}

	@Override
	public final ISerialPortStream Reset(ISerialPortStream serialPort, SerialPortConfig config) {
		serialPort.setDtrEnable(getToggle());
		return serialPort;
	}
}