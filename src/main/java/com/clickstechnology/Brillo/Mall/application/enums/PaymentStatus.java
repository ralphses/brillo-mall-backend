package com.clickstechnology.Brillo.Mall.application.enums;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    PAID,
    FAILED,
    REVERSED;

    public boolean canTransitionTo(PaymentStatus nextStatus) {
        if (nextStatus == null) {
            return false;
        }

        if (this == nextStatus) {
            return true;
        }

        return switch (this) {
            case PENDING -> nextStatus == PROCESSING || nextStatus == PAID || nextStatus == FAILED;
            case PROCESSING -> nextStatus == PAID || nextStatus == FAILED;
            case FAILED -> nextStatus == PROCESSING;
            case PAID -> nextStatus == REVERSED;
            case REVERSED -> false;
        };
    }

    public boolean isTerminal() {
        return this == REVERSED;
    }
}
