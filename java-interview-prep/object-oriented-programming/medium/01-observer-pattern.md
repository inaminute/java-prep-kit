# Observer Pattern

## Problem Statement

Implement the Observer design pattern to create a weather monitoring system where multiple display devices (observers) automatically update when weather data (subject) changes. The pattern should support dynamic subscription/unsubscription and demonstrate loose coupling between the subject and observers.

**Requirements:**
- Create a Subject interface with attach, detach, and notify methods
- Create an Observer interface with update method
- Implement a WeatherStation as the concrete subject
- Implement multiple display types as concrete observers
- Support adding and removing observers dynamically
- Demonstrate automatic notification on state change

## Approach

- Define Subject and Observer interfaces for loose coupling
- Maintain a list of observers in the subject
- Implement attach() to add observers and detach() to remove them
- Call notify() when subject state changes, which calls update() on all observers
- Pass necessary data to observers through update() method
- Observers pull additional data if needed or receive it via push

## Solution

```java
import java.util.ArrayList;
import java.util.List;

// Observer interface
interface Observer {
    void update(float temperature, float humidity, float pressure);
}

// Subject interface
interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers();
}

// Concrete Subject - WeatherStation
class WeatherStation implements Subject {
    private List<Observer> observers;
    private float temperature;
    private float humidity;
    private float pressure;
    
    public WeatherStation() {
        observers = new ArrayList<>();
    }
    
    @Override
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("Observer attached: " + observer.getClass().getSimpleName());
        }
    }
    
    @Override
    public void detach(Observer observer) {
        if (observers.remove(observer)) {
            System.out.println("Observer detached: " + observer.getClass().getSimpleName());
        }
    }
    
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(temperature, humidity, pressure);
        }
    }
    
    // Called when weather measurements change
    public void setMeasurements(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        measurementsChanged();
    }
    
    private void measurementsChanged() {
        notifyObservers();
    }
    
    // Getters for pull model
    public float getTemperature() { return temperature; }
    public float getHumidity() { return humidity; }
    public float getPressure() { return pressure; }
}

// Concrete Observer - Current Conditions Display
class CurrentConditionsDisplay implements Observer {
    private float temperature;
    private float humidity;
    
    @Override
    public void update(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        display();
    }
    
    public void display() {
        System.out.println("Current conditions: " + temperature + "°F and " + humidity + "% humidity");
    }
}

// Concrete Observer - Statistics Display
class StatisticsDisplay implements Observer {
    private float maxTemp = Float.MIN_VALUE;
    private float minTemp = Float.MAX_VALUE;
    private float tempSum = 0.0f;
    private int numReadings = 0;
    
    @Override
    public void update(float temperature, float humidity, float pressure) {
        tempSum += temperature;
        numReadings++;
        
        if (temperature > maxTemp) {
            maxTemp = temperature;
        }
        if (temperature < minTemp) {
            minTemp = temperature;
        }
        
        display();
    }
    
    public void display() {
        System.out.println("Avg/Max/Min temperature: " + (tempSum / numReadings) + 
                         "/" + maxTemp + "/" + minTemp);
    }
}

// Concrete Observer - Forecast Display
class ForecastDisplay implements Observer {
    private float currentPressure = 29.92f;
    private float lastPressure;
    
    @Override
    public void update(float temperature, float humidity, float pressure) {
        lastPressure = currentPressure;
        currentPressure = pressure;
        display();
    }
    
    public void display() {
        System.out.print("Forecast: ");
        if (currentPressure > lastPressure) {
            System.out.println("Improving weather on the way!");
        } else if (currentPressure == lastPressure) {
            System.out.println("More of the same");
        } else {
            System.out.println("Watch out for cooler, rainy weather");
        }
    }
}

// Demo class
class ObserverPatternDemo {
    public static void main(String[] args) {
        // Create subject
        WeatherStation weatherStation = new WeatherStation();
        
        // Create observers
        CurrentConditionsDisplay currentDisplay = new CurrentConditionsDisplay();
        StatisticsDisplay statisticsDisplay = new StatisticsDisplay();
        ForecastDisplay forecastDisplay = new ForecastDisplay();
        
        // Register observers
        weatherStation.attach(currentDisplay);
        weatherStation.attach(statisticsDisplay);
        weatherStation.attach(forecastDisplay);
        
        // Simulate weather changes
        System.out.println("\n=== Weather Update 1 ===");
        weatherStation.setMeasurements(80, 65, 30.4f);
        
        System.out.println("\n=== Weather Update 2 ===");
        weatherStation.setMeasurements(82, 70, 29.2f);
        
        // Remove an observer
        System.out.println();
        weatherStation.detach(forecastDisplay);
        
        System.out.println("\n=== Weather Update 3 ===");
        weatherStation.setMeasurements(78, 90, 29.2f);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - notifyObservers() iterates through all n observers. attach() and detach() are O(n) due to list operations (contains/remove).

**Space Complexity**: O(n) - The subject maintains a list of n observer references.

## Edge Cases and Pitfalls

- **Concurrent Modification**: If an observer detaches itself during update(), it can cause ConcurrentModificationException. Use CopyOnWriteArrayList or iterate over a copy.
- **Memory Leaks**: Observers that are no longer needed but not detached will prevent garbage collection. Always detach observers when done.
- **Update Order**: Observers are notified in the order they were attached. Don't rely on specific ordering unless explicitly managed.
- **Circular Dependencies**: If observers trigger subject changes during update(), infinite loops can occur. Implement guards to prevent this.
- **Push vs Pull**: Push model (passing all data in update()) is simpler but less flexible. Pull model (observers query subject) gives observers control but creates tighter coupling.
- **Thread Safety**: In multi-threaded environments, synchronize attach(), detach(), and notifyObservers() to prevent race conditions.

## Interview-Ready Answer

"The Observer pattern defines a one-to-many dependency where multiple observers automatically receive notifications when the subject's state changes. I'd create Subject and Observer interfaces, implement WeatherStation as the subject maintaining a list of observers, and create display classes as observers. When weather data changes, the subject calls notifyObservers() which invokes update() on each observer. This achieves loose coupling and supports dynamic subscription. Time complexity is O(n) for notifications, space is O(n) for storing observers."
