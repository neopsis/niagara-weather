
This is the Niagara module implementing the free 5-days weather report provided
by [Openweathermap](https://github.com/neopsis/niagara-weather.git). 

##### Usage

* Get the API key from the openweathemap homepage [here](https://openweathermap.org/appid).
* Install the module in your %niagara_home%/modules directory. 
* Add the Niagara WeatherService to your station's services. 
* Add new provider to the WeatherService - from the list of available
  services select `Nv Owm Provider`. 
* Add the location as `city`  or `city,country_code`, for example `London,GB`

You can download the binary module from the */lib* directory.

For developer: you can find the great documentation 
[here](https://godoc.org/github.com/briandowns/openweathermap)

Enjoy
