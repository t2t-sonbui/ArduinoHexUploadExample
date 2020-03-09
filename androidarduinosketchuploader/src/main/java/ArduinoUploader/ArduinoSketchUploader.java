package ArduinoUploader;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ArduinoUploader.BootloaderProgrammers.ArduinoBootloaderProgrammer;
import ArduinoUploader.BootloaderProgrammers.Protocols.AVR109.Avr109BootloaderProgrammer;
import ArduinoUploader.BootloaderProgrammers.Protocols.STK500v1.Stk500V1BootloaderProgrammer;
import ArduinoUploader.BootloaderProgrammers.Protocols.STK500v2.Stk500V2BootloaderProgrammer;
import ArduinoUploader.BootloaderProgrammers.ResetBehavior.IResetBehavior;
import ArduinoUploader.BootloaderProgrammers.ResetBehavior.ResetThrough1200BpsBehavior;
import ArduinoUploader.BootloaderProgrammers.ResetBehavior.ResetThroughTogglingDtrBehavior;
import ArduinoUploader.BootloaderProgrammers.ResetBehavior.ResetThroughTogglingDtrRtsBehavior;
import ArduinoUploader.BootloaderProgrammers.SerialPortConfig;
import ArduinoUploader.Config.Arduino;
import ArduinoUploader.Config.Configuration;
import ArduinoUploader.Config.McuIdentifier;
import ArduinoUploader.Config.Protocol;
import ArduinoUploader.Hardware.ArduinoModel;
import ArduinoUploader.Hardware.AtMega1284;
import ArduinoUploader.Hardware.AtMega1284P;
import ArduinoUploader.Hardware.AtMega168;
import ArduinoUploader.Hardware.AtMega2560;
import ArduinoUploader.Hardware.AtMega328P;
import ArduinoUploader.Hardware.AtMega32U4;
import ArduinoUploader.Hardware.IMcu;
import CSharpStyle.IProgress;
import IntelHexFormatReader.HexFileReader;
import IntelHexFormatReader.Model.MemoryBlock;
import IntelHexFormatReader.Utils.FileLineIterable;
import IntelHexFormatReader.Utils.LineReader;
import ArduinoUploader.Help.ISerialPortStream;
import csharpstyle.StringHelper;

public class ArduinoSketchUploader<E extends ISerialPortStream> {
    private static IArduinoUploaderLogger Logger;
    private Class<E> inferedClass;
    private final Context mContext;

    public static IArduinoUploaderLogger getLogger() {
        return Logger;
    }

    public static void setLogger(IArduinoUploaderLogger value) {
        Logger = value;
    }

    private ArduinoSketchUploaderOptions _options;
    private IProgress<Double> _progress;

    public ArduinoSketchUploader(Context context, ArduinoSketchUploaderOptions options, IArduinoUploaderLogger logger) {
        this(context, options, logger, null);
    }

    public ArduinoSketchUploader(Context context, ArduinoSketchUploaderOptions options) {
        this(context, options, null, null);
    }

    public ArduinoSketchUploader(Context context, ArduinoSketchUploaderOptions options, IArduinoUploaderLogger logger,
                                 IProgress<Double> progress) {
        this.mContext = context;
        setLogger(logger);
        if (getLogger() != null)
            getLogger().Info("Starting ArduinoSketchUploader...");
        _options = options;
        _progress = progress;
        if (inferedClass == null) {
            try {
                inferedClass = getGenericClass();
            } catch (ClassCastException e) {
                if (getLogger() != null)
                    getLogger().Error("Mus created as anonymous implementation (new Generic<Integer>() {};)...", e);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public ArduinoSketchUploader(Context context, Class<E> clazz, ArduinoSketchUploaderOptions options, IArduinoUploaderLogger logger) {
        this(context, clazz, options, logger, null);
    }

    public ArduinoSketchUploader(Context context, Class<E> clazz, ArduinoSketchUploaderOptions options) {
        this(context, clazz, options, null, null);
    }

    public ArduinoSketchUploader(Context context, Class<E> clazz, ArduinoSketchUploaderOptions options, IArduinoUploaderLogger logger,
                                 IProgress<Double> progress) {
        this.mContext = context;
        setLogger(logger);
        if (getLogger() != null)
            getLogger().Info("Starting ArduinoSketchUploader...");
        _options = options;
        _progress = progress;
        inferedClass = clazz;

    }

    @SuppressWarnings("unchecked")
    public Class<E> getGenericClass() throws ClassNotFoundException {
        if (inferedClass == null) {
            Type mySuperclass = getClass().getGenericSuperclass();
            Type tType = ((ParameterizedType) mySuperclass).getActualTypeArguments()[0];
            String className = tType.toString().split(" ")[1];
            inferedClass = (Class<E>) Class.forName(className);
        }
        // this.inferedClass = ((Class<E>) ((ParameterizedType)
        // getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        return inferedClass;
    }

    public final void UploadSketch() {
        String hexFileName = _options.getFileName();
        Iterable<String> hexFileContents = null;
        if (getLogger() != null)
            getLogger().Info(String.format("Starting upload process for file '%1$s'.", hexFileName));
        try {
            hexFileContents = new FileLineIterable(hexFileName);
        } catch (RuntimeException ex) {
            if (getLogger() != null)
                getLogger().Error(ex.getMessage(), ex);
            throw ex;
        } catch (IOException e) {
            e.printStackTrace();
        }
        UploadSketch(hexFileContents);
    }

    public final void UploadSketch(File hexFile) {
        Iterable<String> hexFileContents = null;
        if (getLogger() != null)
            getLogger().Info(String.format("Starting upload process for file '%1$s'.", hexFile.getAbsoluteFile()));
        try {
            hexFileContents = new FileLineIterable(hexFile);
        } catch (RuntimeException ex) {
            if (getLogger() != null)
                getLogger().Error(ex.getMessage(), ex);
            throw ex;
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Other
//        FileInputStream file = new FileInputStream(myFile);
//        Reader reader = new InputStreamReader(file);
//        Collection<String> lines = new LineReader(reader).readLines();
//        uploader.UploadSketch(lines);

        UploadSketch(hexFileContents);
    }

    public final void UploadSketch(Reader reader) {
        Collection<String> hexFileContents = new LineReader(reader).readLines();
        if (getLogger() != null)
            getLogger().Info(String.format("Starting upload process for InputStreamReader."));
        UploadSketch(hexFileContents);
    }


    public final void UploadSketch(Iterable<String> hexFileContents) {

        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        List<String> portNames = new ArrayList<>();
        for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
            UsbDevice usbDevice = entry.getValue();
            int deviceVID = usbDevice.getVendorId();
            int devicePID = usbDevice.getProductId();
            String deviceName = usbDevice.getDeviceName();
            String deviceKey = entry.getKey();
            if (deviceVID != 0x1d6b && (devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003)) {
                // There is a device connected to our Android device. Try to open it as a Serial Port.
                portNames.add(deviceKey);
                System.out.println("deviceKey:" + deviceKey);
            }
        }
        String[] allPortNames = portNames.toArray(new String[portNames.size()]);
        try {
            String serialPortName = _options.getPortName();

            Set<String> temp = new HashSet<String>(Arrays.asList(allPortNames));
            String[] uq = temp.toArray(new String[temp.size()]);
            List<String> distinctPorts = Arrays.asList(uq);
            // If we don't specify a COM port, automagically select one if there is only a
            // single match.
            final String portSingleOrDefault;
            if (distinctPorts.size() > 0) {
                portSingleOrDefault = distinctPorts.get(0);
            } else
                portSingleOrDefault = null;

            if (StringHelper.isNullOrWhiteSpace(serialPortName) && portSingleOrDefault != null) {
                if (getLogger() != null)
                    getLogger().Info(String.format("Port autoselected: %1$s.", serialPortName));
                serialPortName = distinctPorts.get(0);
            }
            // Or else, check that we have an unambiguous match. Throw an exception
            // otherwise.
            else if ((allPortNames.length == 0) || !distinctPorts.contains(serialPortName)) {
                throw new ArduinoUploaderException(
                        String.format("Specified COM port name '%1$s' is not valid.", serialPortName));
            }
            if (getLogger() != null)
                getLogger().Trace(String.format("Creating serial port '%1$s'...", serialPortName));
            ArduinoBootloaderProgrammer<E> programmer;
            IMcu mcu;
            String model = _options.getArduinoModel().toString();
            Configuration hardwareConfig = ReadConfiguration();
            Arduino modelOptions = null;
            Arduino[] tempOptions = hardwareConfig.getArduinos();
            for (Arduino arduino : tempOptions) {
                if (arduino.getModel().equalsIgnoreCase(model)) {
                    modelOptions = arduino;
                    break;
                }
            }
            UploadSketch(hexFileContents, modelOptions, serialPortName);
        } catch (RuntimeException ex) {
            if (getLogger() != null)
                getLogger().Error(ex.getMessage(), ex);
            throw ex;
        }
    }

    public final void UploadSketch(Iterable<String> hexFileContents, Arduino modelOptions, String serialPortName) {
        ArduinoBootloaderProgrammer<E> programmer;
        IMcu mcu;
        if (modelOptions == null) {
            throw new ArduinoUploaderException(String.format("Unable to find configuration for '%1$s'!", modelOptions.getModel()));
        }
        switch (modelOptions.getMcu()) {
            case AtMega1284:
                mcu = new AtMega1284();
                break;
            case AtMega1284P:
                mcu = new AtMega1284P();
                break;
            case AtMega2560:
                mcu = new AtMega2560();
                break;
            case AtMega32U4:
                mcu = new AtMega32U4();
                break;
            case AtMega328P:
                mcu = new AtMega328P();
                break;
            case AtMega168:
                mcu = new AtMega168();
                break;
            default:
                throw new ArduinoUploaderException(String.format("Unrecognized MCU: '%1$s'!", modelOptions.getMcu()));
        }
        IResetBehavior preOpenResetBehavior = ParseResetBehavior(modelOptions.getPreOpenResetBehavior());
        IResetBehavior postOpenResetBehavior = ParseResetBehavior(modelOptions.getPostOpenResetBehavior());
        IResetBehavior closeResetBehavior = ParseResetBehavior(modelOptions.getCloseResetBehavior());

        SerialPortConfig serialPortConfig = new SerialPortConfig(serialPortName, modelOptions.getBaudRate(),
                preOpenResetBehavior, postOpenResetBehavior, closeResetBehavior, modelOptions.getSleepAfterOpen(),
                modelOptions.getReadTimeout(), modelOptions.getWriteTimeout());

        switch (modelOptions.getProtocol()) {
            case Avr109:
                if (getLogger() != null)
                    getLogger().Info("Protocol.Avr109");
                programmer = new Avr109BootloaderProgrammer<E>(serialPortConfig, mcu);
                break;
            case Stk500v1:
                if (getLogger() != null)
                    getLogger().Info("Protocol.Stk500v1");
                programmer = new Stk500V1BootloaderProgrammer<E>(serialPortConfig, mcu);
                break;
            case Stk500v2:
                if (getLogger() != null)
                    getLogger().Info("Protocol.Stk500v2");
                programmer = new Stk500V2BootloaderProgrammer<E>(serialPortConfig, mcu);
                break;
            default:
                throw new ArduinoUploaderException(
                        String.format("Unrecognized protocol: '%1$s'!", modelOptions.getProtocol()));
        }

        try {
            if (getLogger() != null)
                getLogger().Info("Establishing memory block contents...");
            MemoryBlock memoryBlockContents = ReadHexFile(hexFileContents, mcu.getFlash().getSize());
            programmer.setClazz(inferedClass);
            programmer.setContext(mContext);

            programmer.Open();

            if (getLogger() != null)
                getLogger().Info("Establishing sync...");
            programmer.EstablishSync();
            if (getLogger() != null)
                getLogger().Info("Sync established.");

            if (getLogger() != null)
                getLogger().Info("Checking device signature...");
            programmer.CheckDeviceSignature();
            if (getLogger() != null)
                getLogger().Info("Device signature checked.");

            if (getLogger() != null)
                getLogger().Info("Initializing device...");
            programmer.InitializeDevice();
            if (getLogger() != null)
                getLogger().Info("Device initialized.");

            if (getLogger() != null)
                getLogger().Info("Enabling programming mode on the device...");
            programmer.EnableProgrammingMode();
            if (getLogger() != null)
                getLogger().Info("Programming mode enabled.");

            if (getLogger() != null)
                getLogger().Info("Programming device...");
            programmer.ProgramDevice(memoryBlockContents, _progress);
            if (getLogger() != null)
                getLogger().Info("Device programmed.");

            if (getLogger() != null)
                getLogger().Info("Verifying program...");
            programmer.VerifyProgram(memoryBlockContents, _progress);
            if (getLogger() != null)
                getLogger().Info("Verified program!");

            if (getLogger() != null)
                getLogger().Info("Leaving programming mode...");
            programmer.LeaveProgrammingMode();
            if (getLogger() != null)
                getLogger().Info("Left programming mode!");
        } finally {
            programmer.Close();
        }
        if (getLogger() != null)
            getLogger().Info("All done, shutting down!");

    }

    public final void UploadSketch(String hexFileName, Arduino modelOptions, String serialPortName) {
        Iterable<String> hexFileContents = null;
        if (getLogger() != null)
            getLogger().Info(String.format("Starting upload process for file '%1$s'.", hexFileName));
        try {
            hexFileContents = new FileLineIterable(hexFileName);
        } catch (RuntimeException ex) {
            if (getLogger() != null)
                getLogger().Error(ex.getMessage(), ex);
            throw ex;
        } catch (IOException e) {
            e.printStackTrace();
        }
        UploadSketch(hexFileContents, modelOptions, serialPortName);
    }

    private static MemoryBlock ReadHexFile(Iterable<String> hexFileContents, int memorySize) {
        try {
            HexFileReader reader = new HexFileReader(hexFileContents, memorySize);
            return reader.Parse();
        } catch (RuntimeException ex) {
            if (getLogger() != null)
                getLogger().Error(ex.getMessage(), ex);
            throw ex;
        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;
    }

    private static Configuration ReadConfiguration() {// Todo add new arduino dynamic
        Configuration hardwareConfig = new Configuration();
        List<Arduino> listArduino = new ArrayList<>();

        Arduino Leonardo = new Arduino(ArduinoModel.Leonardo.toString(), McuIdentifier.AtMega32U4, 57600,
                Protocol.Avr109);
        Leonardo.setPreOpenResetBehavior("1200bps");
        Leonardo.setSleepAfterOpen(0);
        Leonardo.setReadTimeout(1000);
        Leonardo.setWriteTimeout(1000);
        listArduino.add(Leonardo);

        Arduino Mega1284 = new Arduino(ArduinoModel.Mega1284.toString(), McuIdentifier.AtMega1284, 115200,
                Protocol.Stk500v1);
        Mega1284.setPreOpenResetBehavior("DTR;true");
        Mega1284.setCloseResetBehavior("DTR-RTS;250;50");
        Mega1284.setSleepAfterOpen(250);
        Mega1284.setReadTimeout(1000);
        Mega1284.setWriteTimeout(1000);
        listArduino.add(Mega1284);

        Arduino Mega2560 = new Arduino(ArduinoModel.Mega2560.toString(), McuIdentifier.AtMega2560, 115200,
                Protocol.Stk500v2);
        Mega2560.setPostOpenResetBehavior("DTR-RTS;50;250;true");
        Mega2560.setCloseResetBehavior("DTR-RTS;250;50;true");
        Mega2560.setSleepAfterOpen(250);
        Mega2560.setReadTimeout(1000);
        Mega2560.setWriteTimeout(1000);
        listArduino.add(Mega2560);

        Arduino Micro = new Arduino(ArduinoModel.Micro.toString(), McuIdentifier.AtMega32U4, 57600, Protocol.Avr109);
        Micro.setPreOpenResetBehavior("1200bps");
        Micro.setSleepAfterOpen(0);
        Micro.setReadTimeout(1000);
        Micro.setWriteTimeout(1000);
        listArduino.add(Micro);

        Arduino NanoR2 = new Arduino(ArduinoModel.NanoR2.toString(), McuIdentifier.AtMega168, 19200, Protocol.Stk500v1);
        NanoR2.setPreOpenResetBehavior("DTR;true");
        NanoR2.setCloseResetBehavior("DTR-RTS;250;50");
        NanoR2.setSleepAfterOpen(250);
        NanoR2.setReadTimeout(1000);
        NanoR2.setWriteTimeout(1000);
        listArduino.add(NanoR2);

        Arduino NanoR3 = new Arduino(ArduinoModel.NanoR3.toString(), McuIdentifier.AtMega328P, 57600,
                Protocol.Stk500v1);
        NanoR3.setPreOpenResetBehavior("DTR;true");
        NanoR3.setCloseResetBehavior("DTR-RTS;250;50");
        NanoR3.setSleepAfterOpen(250);
        NanoR3.setReadTimeout(1000);
        NanoR3.setWriteTimeout(1000);
        listArduino.add(NanoR3);

        Arduino UnoR3 = new Arduino(ArduinoModel.UnoR3.toString(), McuIdentifier.AtMega328P, 115200, Protocol.Stk500v1);
        UnoR3.setPreOpenResetBehavior("DTR;true");
        UnoR3.setCloseResetBehavior("DTR-RTS;50;250;false");
        UnoR3.setSleepAfterOpen(250);
        UnoR3.setReadTimeout(1000);
        UnoR3.setWriteTimeout(1000);
        listArduino.add(UnoR3);
        hardwareConfig.setArduinos(listArduino.toArray(new Arduino[listArduino.size()]));
        return hardwareConfig;
    }

    private IResetBehavior ParseResetBehavior(String resetBehavior) {
        if (resetBehavior == null) {
            return null;
        }
        if (resetBehavior.trim().equalsIgnoreCase("1200bps")) {
            return new ResetThrough1200BpsBehavior<E>(inferedClass, mContext);
        }

        String[] parts = resetBehavior.split(";", -1);
        int numberOfParts = parts.length;
        if (numberOfParts == 2 && parts[0].trim().equalsIgnoreCase("DTR")) {
            boolean flag = parts[1].trim().equalsIgnoreCase("true");
            return new ResetThroughTogglingDtrBehavior(flag);
        }

        if (numberOfParts < 3 || numberOfParts > 4) {
            throw new ArduinoUploaderException(
                    String.format("Unexpected format (%1$s parts to '%2$s')!", numberOfParts, resetBehavior));
        }

        // Only DTR-RTS supported at this point...
        String type = parts[0];
        if (!type.equalsIgnoreCase("DTR-RTS")) {
            throw new ArduinoUploaderException(
                    String.format("Unrecognized close reset behavior: '%1$s'!", resetBehavior));
        }

        int wait1, wait2;
        try {
            wait1 = Integer.parseInt(parts[1]);
        } catch (RuntimeException e) {
            throw new ArduinoUploaderException(String.format("Unrecognized Wait (1) in DTR-RTS: '%1$s'!", parts[1]));
        }

        try {
            wait2 = Integer.parseInt(parts[2]);
        } catch (RuntimeException e2) {
            throw new ArduinoUploaderException(String.format("Unrecognized Wait (2) in DTR-RTS: '%1$s'!", parts[2]));
        }

        boolean inverted = numberOfParts == 4 && parts[3].equalsIgnoreCase("true");
        return new ResetThroughTogglingDtrRtsBehavior(wait1, wait2, inverted);
    }

    private static IResetBehavior ParseCloseResetBehavior(String closeResetBehavior) {
        if (closeResetBehavior == null) {
            return null;
        }
        String[] parts = closeResetBehavior.split(";", -1);
        int numberOfParts = parts.length;
        if (numberOfParts < 3 || numberOfParts > 4) {
            throw new ArduinoUploaderException(
                    String.format("Unexpected format (%1$s parts to '%2$s')!", numberOfParts, closeResetBehavior));
        }
        // Only DTR-RTS supported at this point...
        String type = parts[0];
        if (!type.equalsIgnoreCase("DTR-RTS")) {
            throw new ArduinoUploaderException(
                    String.format("Unrecognized close reset behavior: '%1$s'!", closeResetBehavior));
        }

        int wait1, wait2;
        try {
            wait1 = Integer.parseInt(parts[1]);
        } catch (RuntimeException e) {
            throw new ArduinoUploaderException(String.format("Unrecognized Wait (1) in DTR-RTS: '%1$s'!", parts[1]));
        }

        try {
            wait2 = Integer.parseInt(parts[2]);
        } catch (RuntimeException e2) {
            throw new ArduinoUploaderException(String.format("Unrecognized Wait (2) in DTR-RTS: '%1$s'!", parts[2]));
        }

        boolean inverted = numberOfParts == 4 && parts[3].equalsIgnoreCase("true");
        return new ResetThroughTogglingDtrRtsBehavior(wait1, wait2, inverted);
    }

}