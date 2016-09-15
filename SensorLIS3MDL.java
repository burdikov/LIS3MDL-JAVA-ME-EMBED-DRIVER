import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import jdk.dio.DeviceManager;
import jdk.dio.i2cbus.I2CDevice;
import jdk.dio.i2cbus.I2CDeviceConfig;

/**
 *
 * @author Леонид Бурдиков, leonid.b.d@gmail.com
 */
public class SensorLIS3MDL {
    private final I2CDeviceConfig conf;
    private I2CDevice dev;
    
    private boolean temp_en;
    private boolean fast_odr;
    
    /**Creates new instance of this class.
     * <p>This parameterized constructor allows you to specify the 7-bit address
     * of the device, number of the I2C Bus controller and clock frequency.
     * @param address Either 28 (0x38) or 30 (0x3c).
     * @param controllerNumber Usually 1.
     * @param clockFrequency Either 100000 or 400000 Hz.
     */
    SensorLIS3MDL(int address, int controllerNumber, int clockFrequency){
        conf = new I2CDeviceConfig.Builder()
                .setControllerNumber(controllerNumber)
                .setAddress(address, I2CDeviceConfig.ADDR_SIZE_7)
                .setClockFrequency(clockFrequency)
                .build();
        temp_en = false;
    }
    
    /**Creates new instance of this class.
     * <p>This parameterized constructor allows you to specify only the 7-bit
     * address of the device. Controller number is set to 1 and clock frequency
     * is set to 100000 Hz.
     * 
     * @param address Either 28 (0x38) or 30 (0x3c).
     */
    SensorLIS3MDL(int address){
        this(address, 1, 100000);
    }
    
    /**Creates new instance of this class.
     * <p>This constructor sets address of the device to the 28 (0x38), 
     * controller number to 1 and clock frequency to 100000 Hz.
     * 
     */
    SensorLIS3MDL(){
        this(28);
    }
    
    /**Gets the WHO_AM_I register value.
     * <p>LIS3MDL must return 61. If it's not the case try another bus address
     * of the device.
     * @return 61.
     * @throws IOException 
     */
    public byte whoAmI() throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x0f,1,buf);
        dev.close();
        return buf.get(0);
    }
    
    /**Enable or disable a temperature sensor.
     * 
     * @param enable True - enabled.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setTemperatureSensor(boolean enable) throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x20,1,buf);
        byte reg = buf.get(0);
        if (enable) reg |= 0b1000_0000; else reg &= 0b0111_1111;
        buf.put(0,reg).rewind();
        dev.write(0x20,1,buf);
        dev.close();
        return this;
    }
    
    public static final int AXIS_OM_LP = 0;
    public static final int AXIS_OM_MP = 1;
    public static final int AXIS_OM_HP = 2;
    public static final int AXIS_OM_UHP = 3;
    
    /**Set X and Y axes operating mode.
     * <p>Device supports four operating modes:
     * <p> - Low-power;
     * <p> - Medium-performance;
     * <p> - High-performance;
     * <p> - Ultra-high-performance.
     * @param mode One of the AXIS_OM static constants.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setOperativeModeXY(int mode) throws IOException{
        if (mode < 0 | mode > 3) throw new IllegalArgumentException();
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x20,1,buf);
        byte reg = (byte) (buf.get(0) & 0b1001_1111 | (mode << 5));
        buf.put(0,reg).rewind();
        dev.write(0x20,1,buf);
        dev.close();
        return this;
    }
    
    /**Set Z axis operating mode.
     * <p>Device supports four operating modes:
     * <p> - Low-power;
     * <p> - Medium-performance;
     * <p> - High-performance;
     * <p> - Ultra-high-performance.
     * @param mode One of the AXIS_OM static constants.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setOperativeModeZ(int mode) throws IOException{
        if (mode < 0 | mode > 3) throw new IllegalArgumentException();
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x23,1,buf);
        byte reg = (byte) (buf.get(0) & 0b1111_0011 | mode);
        buf.put(0,reg).rewind();
        dev.write(0x23,1,buf);
        dev.close();
        return this;
    }
    
    public static final int ODR_0p625 = 0;
    public static final int ODR_1p25 = 1;
    public static final int ODR_2p5 = 2;
    public static final int ODR_5 = 3;
    public static final int ODR_10 = 4;
    public static final int ODR_20 = 5;
    public static final int ODR_40 = 6;
    public static final int ODR_80 = 7;
    
    /**Set the Output Data Rate of the device.
     * <p>Device supports eight different ODR values. Use static ODR_n constants
     * where n is the rate in Hz.
     * <p>Note: this value only impacts if the FAST_ODR is not enabled.
     * @param rate One of the ODR constants.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setODR(int rate) throws IOException{
        if (rate < 0 | rate > 7) throw new IllegalArgumentException();
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x20,1,buf);
        byte reg = (byte) (buf.get(0) & 0b1110_0011 | (rate << 2));
        buf.put(0,reg).rewind();
        dev.write(0x20,1,buf);
        dev.close();
        return this;
    }
    
    /**Enable or disable FAST_ODR.
     * <p>Fast ODR enables data rates higher than 80 Hz. When this function is
     * enabled ODR are defined by operative mode set:
     * <p> LP - 1000 Hz;
     * <p> MP - 560 Hz;
     * <p> HP - 300 Hz;
     * <p> UHP - 155 Hz;
     * @param enable True - Fast ODR enabled.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setFastODR(boolean enable) throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x20,1,buf);
        byte reg = buf.get(0);
        if (enable) reg |= 0b0000_0010; else reg &= 0b1111_1101;
        buf.put(0,reg).rewind();
        dev.write(0x20,1,buf);
        dev.close();
        fast_odr = enable;
        return this;
    }
    
    /**Self-test of the device.
     * Refer to the datasheet of the device to more information.
     * @throws IOException 
     */
    public void SelfTest() throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x20,1,buf);
        byte reg = (byte) (buf.get(0) | 0b0000_0001);
        buf.put(0,reg).rewind();
        dev.write(0x20,1,buf);
        dev.close();
    }
    
    public static final int FS_4 = 0;
    public static final int FS_8 = 1;
    public static final int FS_12 = 2;
    public static final int FS_16 = 3;
    
    /**Full-scale configuration.
     * <p> Four options are present: ±4 gauss, ±8 gauss, ±12 gauss, ±16 gauss.
     * @param fullScale One of the FS constants.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setFullScale(int fullScale) throws IOException{
        if (fullScale < 0 | fullScale > 3) throw new IllegalArgumentException();
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x21,1,buf);
        byte reg = (byte) (buf.get(0) & 0b1001_1111 | (fullScale << 5));
        buf.put(0,reg).rewind();
        dev.write(0x21,1,buf);
        dev.close();
        return this;
    }
    
    /**Reboot memory content of the device.
     * 
     * @throws IOException
     * @throws InterruptedException 
     */
    public void reboot() throws IOException, InterruptedException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x21,1,buf);
        byte reg = (byte) (buf.get(0) | 0b0000_1000);
        buf.put(0,reg).rewind();
        dev.write(0x21,1,buf);
        Thread.sleep(100);
        dev.close();
    }
    
    /**Configuration registers and user registers reset function.
     * 
     * @throws IOException
     * @throws InterruptedException 
     */
    public void softReset() throws IOException, InterruptedException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x21,1,buf);
        byte reg = (byte) (buf.get(0) | 0b0000_0100);
        buf.put(0,reg).rewind();
        dev.write(0x21,1,buf);
        Thread.sleep(100);
        dev.close();
    }
    
    /**Enable or disable low-power mode.
     * <p>Once this mode is enabled, ODR is set to the 0.625 Hz and device
     * performs minimum number of averages for each channel.
     * @param enable True - enabled.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setLowPower(boolean enable) throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x22,1,buf);
        byte reg = buf.get(0);
        if (enable) reg |= 0b0010_0000; else reg &= 0b1101_1111;
        buf.put(0,reg).rewind();
        dev.write(0x22,1,buf);
        dev.close();
        return this;
    }
    
    /**SPI serial interface mode selection.
     * <p>Either three or four wire.
     * @param threeWire True - three-wire interface active.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setSIM(boolean threeWire) throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x22,1,buf);
        byte reg = buf.get(0);
        if (threeWire) reg |= 0b0000_0100; else reg &= 0b1111_1011;
        buf.put(0,reg).rewind();
        dev.write(0x22,1,buf);
        dev.close();
        return this;
    }
    
    public static final int OM_CONT_CONV = 0;
    public static final int OM_SING_CONV = 1;
    public static final int OM_POW_DOWN = 2;
    
    /**Set operating mode of the device.
     * <p>Three options are available:
     * <p> - Continuous-conversion mode;
     * <p> - Single-conversion mode;
     * <p> - Power-down.
     * <p>Single conversion mode has to be used with ODR from 0.625 Hz to 80 Hz.
     * @param mode One of the OM static constants.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setMode(int mode) throws IOException{
        if (mode < 0 | mode > 3) throw new IllegalArgumentException();
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x22,1,buf);
        byte reg = (byte) (buf.get(0) & 0b1111_1100 | mode);
        buf.put(0,reg).rewind();
        dev.write(0x22,1,buf);
        dev.close();
        return this;
    }
    
    private void setBigLittleEndian(boolean big) throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x23,1,buf);
        byte reg = buf.get(0);
        if (big) reg |= 0b0000_0010; else reg &= 0b1111_1101;
        buf.put(0,reg).rewind();
        dev.write(0x23,1,buf);
        dev.close();
    }
    
    /**Enable or disable fast read.
     * <p>Fast read allows reading the high part of DATA OUT only in order to
     * increase reading efficiency.
     * @param enable True - enabled.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setFastRead(boolean enable) throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x24,1,buf);
        byte reg = buf.get(0);
        if (enable) reg |= 0b1000_0000; else reg &= 0b0111_1111;
        buf.put(0,reg).rewind();
        dev.write(0x24,1,buf);
        dev.close();
        return this;
    }
    
    /**Enable or disable block data update.
     * <p>The BDU bit is used to inhibit the output register update between the reading of the upper
     * and lower register parts. In default mode (BDU = ‘0’), the lower and upper register parts are
     * updated continuously. If it is not certain whether the read will be faster than output data rate,
     * it is recommended to set the BDU bit to ‘1’. In this way, after the reading of the lower (upper)
     * register part, the content of that output register is not updated until the upper (lower) part is
     * read also.
     * 
     * <p>This feature prevents the reading of LSB and MSB related to different samples.
     * @param enable True - enabled.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setBDU(boolean enable) throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x24,1,buf);
        byte reg = buf.get(0);
        if (enable) reg |= 0b0100_0000; else reg &= 0b1011_1111;
        buf.put(0,reg).rewind();
        dev.write(0x24,1,buf);
        dev.close();
        return this;
    }
    
    public static final int INT_EN_X = 4;
    public static final int INT_EN_Y = 2;
    public static final int INT_EN_Z = 1;
    public static final int INT_DISABLED = 0;
    
    /**Decide on which events interrrupt will be generated.
     * <p>Device generates interrupt when output value on channel exceeds trashold.
     * You can enable interrupt generation for each channel.
     * @param conf Bitwise combination of INT_EN constants or INT_DISABLED.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setInterruptConf(int conf) throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(this.conf);
        dev.read(0x30,1,buf);
        byte reg = buf.get(0);
        if (conf == 0)
            reg &= 0b0001_1110;
        else
            reg |= (conf << 5) | 0b0000_0001;
        buf.put(0,reg).rewind();
        dev.write(0x30,1,buf);
        dev.close();
        return this;
    }
    
    /**Active level configuration on interrupt pin.
     * 
     * @param high True - high active level.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setInterruptActive(boolean high) throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x30,1,buf);
        byte reg = buf.get(0);
        if (high) reg |= 0b0000_0100; else reg &= 0b1111_1011;
        buf.put(0,reg).rewind();
        dev.write(0x30,1,buf);
        dev.close();
        return this;
    }
    
    /**Whether to latch interrupt request or not.
     * 
     * @param latched True - request is latched.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setInterruptLatchRequest(boolean latched) throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(1);
        dev = DeviceManager.open(conf);
        dev.read(0x30,1,buf);
        byte reg = buf.get(0);
        if (latched) reg |= 0b0000_0010; else reg &= 0b1111_1101;
        buf.put(0,reg).rewind();
        dev.write(0x30,1,buf);
        dev.close();
        return this;
    }
    
    /**Get current X-axis data from the device.
     * 
     * @return
     * @throws IOException 
     */
    public short getX() throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(2);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        dev = DeviceManager.open(conf);
        dev.read(0xA8,1,buf);
        dev.close();
        return buf.getShort(0);
    }
    
    /**Get current Y-axis data from the device.
     * 
     * @return
     * @throws IOException 
     */
    public short getY() throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(2);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        dev = DeviceManager.open(conf);
        dev.read(0xAA,1,buf);
        dev.close();
        return buf.getShort(0);
    }
    
    /**Get current Z-axis data from the device.
     * 
     * @return
     * @throws IOException 
     */
    public short getZ() throws IOException{
        ByteBuffer buf = ByteBuffer.allocateDirect(2);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        dev = DeviceManager.open(conf);
        dev.read(0xAC,1,buf);
        dev.close();
        return buf.getShort(0);
    }
    
    /**Set interrupt generation treshold.
     * 
     * @param value Positive unsigned value.
     * @return
     * @throws IOException 
     */
    public SensorLIS3MDL setIntTreshold(short value) throws IOException{
        if (value < 0) throw new IllegalArgumentException("Only positive value allowed.");
        
        ByteBuffer buf = ByteBuffer.allocateDirect(2);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort(0,value);
        
        dev = DeviceManager.open(conf);
        dev.write(0xb2,1,buf);
        dev.close();
        
        return this;
    }
}
