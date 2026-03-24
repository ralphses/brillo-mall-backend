package com.clickstechnology.Brillo.Mall.application.enums;

import java.util.List;
import java.util.Optional;

public enum InvitationMessage {

    SCHOOL_TO_TEACHER {
        @Override
        public String generateMessage(String inviter, Optional<List<String>> children) {
            return String.format(
                    "%s has invited you to connect on FutureHive to support student academic and career development. " +
                            "Tap the button below to review the request and proceed.",
                    inviter
            );
        }
    },

    SCHOOL_TO_PARENT {
        @Override
        public String generateMessage(String inviter, Optional<List<String>> children) {
            String childrenText = children.map(list -> String.join(", ", list))
                    .orElse("your");
            return String.format(
                    "%s has invited you to connect on FutureHive to stay informed about %s academic and career progress. " +
                            "Tap the button below to review the request and proceed.",
                    inviter, childrenText
            );
        }
    },

    SCHOOL_TO_STUDENT {
        @Override
        public String generateMessage(String inviter, Optional<List<String>> children) {
            return String.format(
                    "%s has invited you to connect on FutureHive to access academic resources, career guidance, and personalized development tools. " +
                            "Tap the button below to review the request and proceed.",
                    inviter
            );
        }
    },

    PARENT_TO_TEACHER {
        @Override
        public String generateMessage(String inviter, Optional<List<String>> children) {
            String childrenText = children.map(list -> String.join(", ", list))
                    .orElse("");
            String childPart = childrenText.isEmpty() ? "" : childrenText + "'s ";
            return String.format(
                    "%s has requested to engage your services on FutureHive to support %sacademic and career development. " +
                            "Tap the button below to review the request and proceed.",
                    inviter, childPart
            );
        }
    },

    STUDENT_TO_TEACHER {
        @Override
        public String generateMessage(String inviter, Optional<List<String>> children) {
            return String.format(
                    "%s has requested to connect with you on FutureHive for academic support and career guidance. " +
                            "Tap the button below to review the request and proceed.",
                    inviter
            );
        }
    };

    /**
     * Generate message for the flow.
     * @param inviter Name of the user sending the invitation/request
     * @param children Optional list of children (can be empty)
     * @return The formatted message
     */
    public abstract String generateMessage(String inviter, Optional<List<String>> children);
}
