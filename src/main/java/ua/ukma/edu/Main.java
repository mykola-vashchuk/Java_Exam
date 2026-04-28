package ua.ukma.edu;

import ua.ukma.edu.service.ClinicScheduleService;

public class Main {
    public static void main(String[] args) {
            ClinicScheduleService clinicScheduleService = new ClinicScheduleService();
            clinicScheduleService.process();
        }
    }
