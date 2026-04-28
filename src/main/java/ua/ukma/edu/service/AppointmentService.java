package ua.ukma.edu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.ukma.edu.domain.Appointment;
import ua.ukma.edu.domain.Doctor;
import ua.ukma.edu.domain.Patient;
import ua.ukma.edu.domain.Status;
import ua.ukma.edu.domain.TimeSlot;
import ua.ukma.edu.exceptions.AppointmentException;
import ua.ukma.edu.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AppointmentService implements ProcessInterface {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final Repository<Doctor, String> doctorRepository;
    private final Repository<Patient, String> patientRepository;
    private final Repository<Appointment, String> appointmentRepository;

    public AppointmentService() {
        this(new InMemoryRepository<>(Doctor::id), new InMemoryRepository<>(Patient::id), new InMemoryRepository<>(Appointment::id));
    }

    public AppointmentService(Repository<Doctor, String> doctorRepository,
                              Repository<Patient, String> patientRepository,
                              Repository<Appointment, String> appointmentRepository) {
        this.doctorRepository = Objects.requireNonNull(doctorRepository, "doctorRepository");
        this.patientRepository = Objects.requireNonNull(patientRepository, "patientRepository");
        this.appointmentRepository = Objects.requireNonNull(appointmentRepository, "appointmentRepository");
    }

    public Doctor registerDoctor(Doctor doctor) {
        Objects.requireNonNull(doctor, "doctor");
        doctorRepository.save(doctor);
        return doctor;
    }

    public Patient registerPatient(Patient patient) {
        Objects.requireNonNull(patient, "patient");
        patientRepository.save(patient);
        return patient;
    }

    public Optional<Doctor> findDoctor(String doctorId) {
        return doctorRepository.findById(requireId(doctorId, "doctorId"));
    }

    public Optional<Patient> findPatient(String patientId) {
        return patientRepository.findById(requireId(patientId, "patientId"));
    }

    public Optional<Appointment> findAppointment(String appointmentId) {
        return appointmentRepository.findById(requireId(appointmentId, "appointmentId"));
    }

    public Appointment createAppointment(String appointmentId, String doctorId, String patientId, TimeSlot timeSlot) {
        String id = requireId(appointmentId, "appointmentId");
        if (appointmentRepository.existsById(id)) {
            throw new ValidationException("Appointment already exists: " + id);
        }

        Doctor doctor = findDoctor(doctorId).orElseThrow(() -> new ValidationException("Unknown doctor: " + doctorId));
        Patient patient = findPatient(patientId).orElseThrow(() -> new ValidationException("Unknown patient: " + patientId));

        validateAppointmentCreation(doctor, patient, timeSlot);

        Appointment created = new Appointment(id, doctor, patient, timeSlot, Status.CREATED, LocalDateTime.now(), LocalDateTime.now())
                .confirm();
        appointmentRepository.save(created);
        log.info("Created appointment {} for doctor {} and patient {}", id, doctor.id(), patient.id());
        return created;
    }

    public Appointment confirmAppointment(String appointmentId) {
        Appointment appointment = requireAppointment(appointmentId);
        Appointment confirmed = appointment.confirm();
        appointmentRepository.save(confirmed);
        return confirmed;
    }

    public Appointment rescheduleAppointment(String appointmentId, TimeSlot newSlot) {
        Appointment appointment = requireAppointment(appointmentId);
        if (!appointment.isActive()) {
            throw new AppointmentException("Only active appointments can be rescheduled.");
        }
        validateSlot(newSlot);
        ensureNoConflict(appointment.doctor(), newSlot, appointment.id());
        Appointment updated = appointment.reschedule(newSlot);
        appointmentRepository.save(updated);
        log.info("Rescheduled appointment {} to {}", appointmentId, newSlot);
        return updated;
    }

    public Appointment cancelAppointment(String appointmentId) {
        Appointment appointment = requireAppointment(appointmentId);
        Appointment cancelled = appointment.cancel();
        appointmentRepository.save(cancelled);
        log.info("Cancelled appointment {}", appointmentId);
        return cancelled;
    }

    public Appointment completeAppointment(String appointmentId) {
        Appointment appointment = requireAppointment(appointmentId);
        Appointment completed = appointment.complete();
        appointmentRepository.save(completed);
        log.info("Completed appointment {}", appointmentId);
        return completed;
    }

    public List<Appointment> findAppointmentsByDoctor(String doctorId) {
        String id = requireId(doctorId, "doctorId");
        return appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.doctor().id().equals(id))
                .sorted(Comparator.comparing(appointment -> appointment.timeSlot().start()))
                .toList();
    }

    public List<Appointment> findAppointmentsByStatus(Status status) {
        Objects.requireNonNull(status, "status");
        return appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.status() == status)
                .sorted(Comparator.comparing(appointment -> appointment.timeSlot().start()))
                .toList();
    }

    public List<Appointment> findActiveAppointmentsSorted() {
        return appointmentRepository.findAll().stream()
                .filter(Appointment::isActive)
                .sorted(Comparator.comparing(appointment -> appointment.timeSlot().start()))
                .toList();
    }

    public Map<String, Long> countAppointmentsByDoctor() {
        return appointmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(appointment -> appointment.doctor().name(), Collectors.counting()));
    }

    public double averageAppointmentDurationMinutes() {
        return appointmentRepository.findAll().stream()
                .mapToLong(appointment -> appointment.timeSlot().durationMinutes())
                .average()
                .orElse(0.0);
    }

    public List<Appointment> findAppointments(Predicate<Appointment> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return appointmentRepository.findAll().stream()
                .filter(predicate)
                .sorted(Comparator.comparing(appointment -> appointment.timeSlot().start()))
                .toList();
    }

    public List<Appointment> findAllAppointments() {
        return appointmentRepository.findAll().stream()
                .sorted(Comparator.comparing(appointment -> appointment.timeSlot().start()))
                .toList();
    }

    @Override
    public void process() {
        log.info("Appointment service ready: {} doctors, {} patients, {} appointments", doctorRepository.findAll().size(), patientRepository.findAll().size(), appointmentRepository.findAll().size());
    }

    private Appointment requireAppointment(String appointmentId) {
        return findAppointment(appointmentId)
                .orElseThrow(() -> new AppointmentException("Unknown appointment: " + appointmentId));
    }

    private void validateAppointmentCreation(Doctor doctor, Patient patient, TimeSlot timeSlot) {
        validateSlot(timeSlot);
        if (!doctor.speciality().canTreat(patient.cure())) {
            throw new ValidationException("Doctor speciality %s cannot treat patient condition %s".formatted(doctor.speciality(), patient.cure()));
        }
        ensureNoConflict(doctor, timeSlot, null);
    }

    private void validateSlot(TimeSlot timeSlot) {
        if (timeSlot == null) {
            throw new ValidationException("Time slot must not be null.");
        }
        if (!timeSlot.isInFuture()) {
            throw new ValidationException("Appointment time slot must be in the future.");
        }
    }

    private void ensureNoConflict(Doctor doctor, TimeSlot timeSlot, String appointmentIdToSkip) {
        boolean conflict = appointmentRepository.findAll().stream()
                .filter(appointment -> appointmentIdToSkip == null || !appointment.id().equals(appointmentIdToSkip))
                .filter(appointment -> appointment.doctor().id().equals(doctor.id()))
                .filter(appointment -> appointment.status().isActive())
                .anyMatch(appointment -> appointment.timeSlot().overlaps(timeSlot));

        if (conflict) {
            throw new AppointmentException("Doctor already has an active appointment during the requested time slot.");
        }
    }

    private String requireId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank.");
        }
        return value;
    }
}
