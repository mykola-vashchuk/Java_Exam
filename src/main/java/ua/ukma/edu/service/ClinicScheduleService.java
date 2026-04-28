package ua.ukma.edu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.ukma.edu.exceptions.ValidationException;

public class ClinicScheduleService implements ProcessInterface{

    private static final Logger log = LoggerFactory.getLogger(ClinicScheduleService.class);

    @Override
    public void process() {
        try{
            log.info("Starting clinic schedule processing...");
            validateSchedule();
            executeScheduleProcessing();
            log.info("Clinic schedule processed successfully.");
        }catch (ValidationException e){
            log.error("Validation error during clinic schedule processing: " + e.getMessage());
        }catch (Exception e){
            log.error("An unexpected error occurred during clinic schedule processing: " + e.getMessage());
        }
        System.out.println("Processing clinic schedule...");

    }

    private void executeScheduleProcessing() {
        log.info("Executing clinic schedule processing...");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error("Clinic schedule processing was interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void validateSchedule() {
        log.info("Validating clinic schedule...");
        boolean isValid = true;
        if (!isValid) {
            throw new ValidationException("Clinic schedule is invalid.");
        }
    }


}
