package tms.network;

import tms.sensors.DemoPressurePad;
import tms.sensors.DemoSpeedCamera;
import tms.sensors.DemoVehicleCount;
import tms.sensors.Sensor;
import tms.util.InvalidNetworkException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class NetworkInitialiser {
    public static final String LINE_INFO_SEPARATOR = ":";
    public static final String LINE_LIST_SEPARATOR = ",";

    public NetworkInitialiser() {
    }

    public static Network loadNetwork​(String filename)
            throws IOException,
            InvalidNetworkException {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        lines = lines.stream().filter(l -> !l.startsWith(";")).collect(Collectors.toList());

        Network network = new Network();

        try {
            int intersectionCount = Integer.valueOf(lines.get(0));
            int routesCount = Integer.valueOf(lines.get(1));
            network.setYellowTime​(Integer.valueOf(lines.get(2)));

            for (int i = 0; i < intersectionCount; i++) {
                var data = lines.get(i + 3).split(":");
                network.createIntersection​(data[0]);
            }

            int skipNumSensors = 0;
            for (int i = 0; i < routesCount; i++) {
                var data = lines.get(i + 3 + intersectionCount + skipNumSensors).split(":");
                network.connectIntersections​(data[0], data[1], Integer.valueOf(data[2]));
                if (data.length == 5) {
                    network.addSpeedSign​(data[0], data[1], Integer.valueOf(data[4]));
                }
                int numSensors = Integer.valueOf(data[3]);
                for (int j = 1; j <= numSensors; j++) {
                    var dataSensor = lines.get(i + 3 + intersectionCount + j + skipNumSensors).split(":");
                    var dataSensorValues = Arrays.stream(dataSensor[2].split(","))
                            .mapToInt(Integer::parseInt)
                            .toArray();
                    Sensor sensor;
                    switch (dataSensor[0]) {
                        case "PP":
                            sensor = new DemoPressurePad(dataSensorValues,
                                    Integer.valueOf(dataSensor[1]));
                            break;
                        case "VC": sensor = new DemoVehicleCount(dataSensorValues,
                                Integer.valueOf(dataSensor[1]));
                            break;
                        case "SC":
                            sensor = new DemoSpeedCamera(dataSensorValues,
                                    Integer.valueOf(dataSensor[1]));
                            break;
                        default: throw new InvalidNetworkException();
                    }
                    network.addSensor​(data[0], data[1], sensor);
                }
                skipNumSensors += numSensors;
            }

            for (int i = 0; i < intersectionCount; i++) {
                var data = lines.get(i + 3).split(":");
                if (data.length > 1) {
                    var order = data[2].split(",");
                    var intersectionOrder = Arrays.asList(order);
                    network.addLights​(data[0], Integer.valueOf(data[1]), intersectionOrder);
                }
            }

            return network;
        } catch (Exception e) {
            throw new InvalidNetworkException(e.getMessage(), e);
        }
    }
}
