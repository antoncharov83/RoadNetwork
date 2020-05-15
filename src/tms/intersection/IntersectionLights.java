package tms.intersection;

import tms.route.Route;
import tms.route.TrafficSignal;
import tms.util.TimedItem;

import java.util.List;

public class IntersectionLights implements TimedItem {
    private List<Route> connections;
    private int yellowTime;
    private int duration;
    private int elapsedSeconds;
    private int currentRoute;

    public IntersectionLights(List<Route> connections, int yellowTime, int duration) {
        this.connections = connections;
        this.yellowTime = yellowTime;
        this.duration = duration;
        this.elapsedSeconds = 0;
        this.currentRoute = 0;
    }

    public int getYellowTime() {
        return yellowTime;
    }

    public void setDuration​(int duration) {
        if (duration > getYellowTime()) {
            this.duration = duration;
        }
    }

    @Override
    public void oneSecond() {
        elapsedSeconds++;
        if(connections.get(currentRoute).getTrafficLight().getSignal() == TrafficSignal.GREEN &&
            duration - yellowTime <= elapsedSeconds) {
            connections.get(currentRoute).getTrafficLight().setSignal(TrafficSignal.YELLOW);
            elapsedSeconds = 0;
        } else if(connections.get(currentRoute).getTrafficLight().getSignal() == TrafficSignal.YELLOW &&
            yellowTime <= elapsedSeconds) {
            connections.get(currentRoute).getTrafficLight().setSignal(TrafficSignal.RED);
            if(currentRoute == connections.size()) {
                currentRoute = 0;
            } else {
                currentRoute++;
            }
            connections.get(currentRoute).getTrafficLight().setSignal(TrafficSignal.GREEN);
        }
    }

    public String toString() {
        String result = duration + ":";
        for(Route route : connections) {
            result += route.getFrom().getId() + ",";
        }

        return result.substring(0, result.length() - 1);
    }
}
