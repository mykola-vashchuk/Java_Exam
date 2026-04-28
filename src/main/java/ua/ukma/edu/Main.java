package ua.ukma.edu;

import ua.ukma.edu.console.ClinicCommandProcessor;
import ua.ukma.edu.service.AppointmentService;
import ua.ukma.edu.service.ClinicScheduleService;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        AppointmentService appointmentService = new AppointmentService();
        ClinicCommandProcessor processor = new ClinicCommandProcessor(appointmentService);
        ClinicScheduleService clinicScheduleService = new ClinicScheduleService(appointmentService);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusMinutes(30);

        processor.execute("REGISTER_DOCTOR D1 Alice FAMILY");
        processor.execute("REGISTER_PATIENT P1 Bob ILL");
        processor.execute("CREATE_APPOINTMENT A1 D1 P1 " + start + " " + end);
        clinicScheduleService.process();
    }
}
