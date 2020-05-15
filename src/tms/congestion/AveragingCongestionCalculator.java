package tms.congestion;

import tms.sensors.Sensor;

import java.util.List;

public class AveragingCongestionCalculator implements CongestionCalculator {
    private List<Sensor> sensors;

    public AveragingCongestionCalculator(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    @Override
    public int calculateCongestion() {
        return sensors.stream().mapToInt(s -> s.getCongestion()).sum();
    }
}
