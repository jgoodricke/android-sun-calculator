package swindroid.suntime.ui;

public class LocationData {
    private String _cityName;
    private double _latitude;
    private double _longitude;
    private String _timeZone;

    public String get_cityName() {
        return _cityName;
    }
    public double get_latitude() {
        return _latitude;
    }
    public double get_longitude() {
        return _longitude;
    }
    public String get_timeZone() {
        return _timeZone;
    }

    public LocationData(String cityName, double latitude, double longitude, String timeZone){
        _cityName = cityName;
        _latitude = latitude;
        _longitude = longitude;
        _timeZone = timeZone;
    }
}

