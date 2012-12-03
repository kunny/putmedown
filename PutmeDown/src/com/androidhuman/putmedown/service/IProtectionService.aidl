package com.androidhuman.putmedown.service;

interface IProtectionService{
	void enableService();
	void disableService();
	void fireWarning();
	void fireAlarm();
	void dismissAlarm();
	void playSound(String soundName);
}