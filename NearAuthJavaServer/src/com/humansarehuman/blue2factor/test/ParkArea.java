package com.humansarehuman.blue2factor.test;

public class ParkArea {
    final int motorcycleSpots = 10;
    final int compactSpots = 20;
    final int largeSpots = 10;

    int carsParked = 0;
    int vansParked = 0;
    int motorcyclesParked = 0;

    int motorcycleSpotsTaken = 0;
    int compactSpotsTaken = 0;
    int largeSpotsTaken = 0;

    int motorcyclesInCompact = 0;
    int motorcyclesInLarge = 0;
    int carsInLarge = 0;

    boolean addVan() {
        boolean success = false;
        if (largeSpots - largeSpotsTaken >= 3) {
            largeSpotsTaken++;
            vansParked++;
            success = true;
        }
        return success;
    }

    boolean addCar() {
        boolean success = false;
        if (compactSpots > carsParked) {
            compactSpotsTaken++;
            carsParked++;
            success = true;
        } else if (largeSpots > largeSpotsTaken) {
            largeSpotsTaken++;
            carsParked++;
            success = true;
        }
        return success;
    }

    boolean addMotorcycle() {
        boolean success = false;
        if (motorcycleSpots > motorcycleSpotsTaken) {
            motorcycleSpotsTaken++;
            motorcyclesParked++;
            success = true;
        } else if (compactSpots > carsParked) {
            compactSpotsTaken++;
            motorcyclesParked++;
            success = true;
        } else if (largeSpots > largeSpotsTaken) {
            largeSpotsTaken++;
            motorcyclesParked++;
            success = true;
        }
        return success;
    }

    int getSpotsRemaining() {
        return getTotalSpots() - getTotalTaken();
    }

    int getTotalTaken() {
        return motorcycleSpotsTaken + compactSpotsTaken + largeSpotsTaken;
    }

    int getTotalSpots() {
        return motorcycleSpots + compactSpots + largeSpots;
    }

    boolean isFull() {
        return getSpotsRemaining() == 0;
    }

    boolean isEmpty() {
        return getSpotsRemaining() == getTotalSpots();
    }

    boolean isMotoSpotFull() {
        return motorcycleSpotsTaken == motorcycleSpots;
    }

    boolean isCompactFull() {
        return compactSpotsTaken == compactSpots;
    }

    boolean isLargeFull() {
        return largeSpotsTaken == largeSpots;
    }

    int vanSpotsTaken() {
        return vansParked * 3;
    }

}
