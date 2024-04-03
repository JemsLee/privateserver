package com.pim.server.client.observer;


public interface PriSubject {
    //Add observer
    void addObserver(PriObserver obj);
    //remove observer
    void deleteObserver(PriObserver obj);
    //When the topic method changes, this method is called to notify all observers
    void notifyObserver(String receiveMessage);
    void notifyErrorObserver(String errorMessage);

}

