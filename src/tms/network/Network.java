package tms.network;

import tms.intersection.Intersection;
import tms.route.Route;
import tms.sensors.Sensor;
import tms.util.DuplicateSensorException;
import tms.util.IntersectionNotFoundException;
import tms.util.InvalidOrderException;
import tms.util.RouteNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Network {
    private List<Intersection> intersections;
    private List<Route> routes;
    private int yellowTime;

    public Network() {
        intersections = new ArrayList<>();
        routes = new ArrayList<>();
    }

    public void addLights​(String intersectionId, int duration, List<String> intersectionOrder)
            throws IntersectionNotFoundException,
            InvalidOrderException,
            IllegalArgumentException {
        Intersection intersectionFound = findIntersection​(intersectionId);

        List<Route> orders = new ArrayList<>();

        for (String order : intersectionOrder) {
            try {
                var route = getConnection​(order, intersectionId);
                orders.add(route);
            } catch (RouteNotFoundException e) {
                throw new InvalidOrderException();
            }
        }

        intersectionFound.addTrafficLights​(orders, this.yellowTime, duration);
    }

    public void addSensor​(String from, String to, Sensor sensor)
            throws DuplicateSensorException,
            IntersectionNotFoundException,
            RouteNotFoundException {
        var route = getConnection​(from, to);

//        if (route.getSensors().stream().anyMatch(s -> s.getClass() == sensor.getClass())) {
//            throw new DuplicateSensorException();
//        }

        route.addSensor(sensor);
    }

    public void addSpeedSign​(String from, String to, int initialSpeed)
            throws IntersectionNotFoundException,
            RouteNotFoundException {
        var route = getConnection​(from, to);
        route.addSpeedSign(initialSpeed);
    }

    public void changeLightDuration​(String intersectionId, int duration)
        throws IntersectionNotFoundException {
        var intersection = findIntersection​(intersectionId);
        intersection.setLightDuration​(duration);
    }

    public void connectIntersections​(String from, String to, int defaultSpeed)
            throws IntersectionNotFoundException,
            IllegalStateException,
            IllegalArgumentException {
        if (defaultSpeed < 0) {
            throw new IllegalArgumentException();
        }

        try {
            getConnection​(from, to);
        } catch (RouteNotFoundException e) {
            var intersectionFrom = findIntersection​(from);
            var intersectionTo = findIntersection​(to);
//            Route route = new Route(from + ":" + to, intersectionFrom, defaultSpeed);
            intersectionTo.addConnection(intersectionFrom, defaultSpeed);
            try {
                Route route = intersectionTo.getConnection(intersectionFrom);
                routes.add(route);
            } catch (RouteNotFoundException routeNotFoundException) {
                throw new IllegalStateException(routeNotFoundException);
            }
//            intersectionTo.getConnections().add(route);
            return;
        }
        throw new IllegalStateException();
    }

    public void createIntersection​(String id) throws IllegalArgumentException {
        if(id == null || id.contains(":") || id.trim().length() == 0) {
            throw new IllegalArgumentException();
        }

        try {
            findIntersection​(id);
        } catch (IntersectionNotFoundException e) {
            intersections.add(new Intersection(id));
            return;
        }

        throw new IllegalArgumentException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Network)) {
            return false;
        }
        return intersections.size() == ((Network) obj).intersections.size() &&
                intersections.containsAll(((Network) obj).intersections);
    }

    public Intersection findIntersection​(String id)
            throws IntersectionNotFoundException {
        var intersectionFrom = intersections.stream()
                .filter(i -> i.getId().equals(id))
                .findAny();

        if (intersectionFrom.isEmpty()) {
            throw new IntersectionNotFoundException();
        }

        return intersectionFrom.get();
    }

    public int getCongestion​(String from, String to)
            throws IntersectionNotFoundException,
            RouteNotFoundException {
        return getConnection​(from, to).getCongestion();
    }

    public Route getConnection​(String from, String to)
            throws IntersectionNotFoundException,
            RouteNotFoundException {
        var intersectionFrom = findIntersection​(from);
        var intersectionTo = findIntersection​(to);
        var routesFrom = routes.stream()
                .filter(r -> r.getFrom().equals(intersectionFrom))
                .collect(Collectors.toList());
        var route = routesFrom.stream()
                .filter(r -> intersectionTo.getConnections().contains(r))
                .findAny();

        if (route.isEmpty()) {
            throw new RouteNotFoundException();
        }

        return route.get();
    }

    public List<Intersection> getIntersections() {
        return intersections;
    }

    public int getYellowTime() {
        return yellowTime;
    }

    @Override
    public int hashCode() {
        int result = 31;
        result += 17 * routes.stream().mapToInt(r -> r.hashCode()).sum();
        result += 17 * intersections.stream().mapToInt(i -> i.hashCode()).sum();
        result += yellowTime;

        return result;
    }

    public void makeTwoWay​(String from, String to)
            throws IntersectionNotFoundException,
            RouteNotFoundException {
        var route = getConnection​(from, to);

        try {
            getConnection​(to, from);
        } catch (RouteNotFoundException e) {
            connectIntersections​(to, from, route.getSpeed());
        }
        throw new IllegalStateException();
    }

    public void setSpeedLimit​(String from, String to, int newLimit)
            throws IntersectionNotFoundException,
            RouteNotFoundException {
        var route = getConnection​(from, to);

        if (!route.hasSpeedSign()) {
            throw new IllegalStateException();
        }

        route.setSpeedLimit(newLimit);
    }

    public void setYellowTime​(int yellowTime) {
        this.yellowTime = yellowTime;
    }

    @Override
    public String toString() {
        String result = intersections.size() + System.lineSeparator() +
                routes.size() +System.lineSeparator() +
                yellowTime + System.lineSeparator();

        for (Intersection i : intersections) {
            result += i.toString();
        }

        for(Route r : routes) {
            result += r.toString();
        }

        return result;
    }
}
