package com.pim.server.client.observer;

import java.util.ArrayList;
import java.util.List;

public class PriManagerSubject implements PriSubject {

    private List<PriObserver> observers=new ArrayList<PriObserver>();

    @Override
    public void addObserver(PriObserver obj) {
        observers.add(obj);
    }

    @Override
    public void deleteObserver(PriObserver obj) {
        int i = observers.indexOf(obj);
        if(i>=0){
            observers.remove(obj);
        }
    }

    @Override
    public void notifyObserver(String receiveMessage) {
        for(int i=0;i<observers.size();i++){
            PriObserver o=(PriObserver)observers.get(i);
            o.onIMMessage(receiveMessage);
        }
    }

    @Override
    public void notifyErrorObserver(String errorMessage) {
        for(int i=0;i<observers.size();i++){
            PriObserver o=(PriObserver)observers.get(i);
            o.onIMError(errorMessage);
        }
    }

    public void publish(String message){
        notifyObserver(message);
    }

    public void publishError(String message){
        notifyErrorObserver(message);
    }
}

