package ua.ukma.edu.domain;

public record Doctor(String id, String name, Speciality speciality) {

	public Doctor {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Doctor id must not be blank.");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Doctor name must not be blank.");
		}
		if (speciality == null) {
			throw new IllegalArgumentException("Doctor speciality must not be null.");
		}
	}
}
