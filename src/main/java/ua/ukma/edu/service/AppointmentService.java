package ua.ukma.edu.service;

import ua.ukma.edu.exceptions.ValidationException;
import java.util.logging.Logger;

public abstract class AppointmentService implements ProcessInterface{
    private static final Logger log = Logger.getLogger(AppointmentService.class.getName());

    @Override
    public void process(){
        try {
           log.info("Process started successfully");
           validate();
           execute();
        } catch (ValidationException e){
            log.warning("Validation error: " + e.getMessage());
            System.out.println("Validation failed: " + e.getMessage());
        } catch (Exception e) {
            log.severe("Unexpected error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    private void validate(){

    }

    private void execute(){}
}
