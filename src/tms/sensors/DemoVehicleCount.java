package tms.sensors;

public class DemoVehicleCount extends DemoSensor
        implements VehicleCount {

    public DemoVehicleCount(int[] data, int threshold) {
        super(data, threshold);
    }

    @Override
    public int countTraffic() {
        return this.getCurrentValue();
    }

    @Override
    public int getCongestion() {
        return countTraffic() / getThreshold();
    }

    @Override
    public String toString() {
        return  "VC:" + getThreshold() + super.toString();
    }
}
