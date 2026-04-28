package ua.ukma.edu.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum Speciality {
    FAMILY(EnumSet.allOf(Cure.class)),
    THERAPY(EnumSet.of(Cure.ILL, Cure.RECOVERING)),
    SURGERY(EnumSet.of(Cure.ILL));

    private final Set<Cure> supportedCures;

    Speciality(Set<Cure> supportedCures) {
        this.supportedCures = Collections.unmodifiableSet(EnumSet.copyOf(supportedCures));
    }

    public boolean canTreat(Cure cure) {
        return supportedCures.contains(cure);
    }

    public Set<Cure> supportedCures() {
        return supportedCures;
    }
}
