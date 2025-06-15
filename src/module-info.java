module GameTime {
	requires java.desktop;
	requires com.google.gson;
	exports com.gametime.core;
	exports com.gametime.input;
	exports com.gametime.objects;
	exports com.gametime.ui;
	exports com.gametime.io;
}