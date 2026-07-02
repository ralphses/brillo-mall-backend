package com.clickstechnology.Brillo.Mall.application.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WhatsappMessageType {
    TEXT("text"),
    TEMPLATE("template"),
    INTERACTIVE("interactive"),
    IMAGE("image"),
    BUTTON("button"),
    LIST("list"),
    FLOW("flow"),
    CTA_URL("cta_url"),
    ADDRESS_MESSAGE("address_message"),
    VOICE_CALL("voice_call"),
    VIDEO("video"),
    DOCUMENT("document"),
    REPLY("reply");


    private final String value;

    WhatsappMessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
